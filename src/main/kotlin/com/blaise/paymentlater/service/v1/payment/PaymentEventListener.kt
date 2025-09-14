package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enum.WebhookEventType
import com.blaise.paymentlater.dto.shared.PaymentIntentConfirmedEventDto
import com.blaise.paymentlater.notification.MailService
import org.springframework.context.event.EventListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.SocketTimeoutException

/**
 * Event listener for payment confirmation events with webhook and email notifications.
 * 
 * Handles payment lifecycle events that require external communication:
 * 
 * **Notification Channels**:
 * - Webhook HTTP callbacks to merchant endpoints
 * - Email notifications for payment confirmations
 * - Asynchronous processing for improved performance
 * 
 * **Reliability Features**:
 * - Automatic retry mechanism for webhook failures
 * - Exponential backoff strategy for resilient delivery
 * - Comprehensive error handling and logging
 * - Non-blocking email and webhook delivery
 * 
 * **Webhook Integration**:
 * - Standardized payload format for merchant systems
 * - Support for various webhook event types
 * - HTTP status validation and error handling
 */
@Component
class PaymentEventListener(
    private val webClient: WebClient,
    private val mailService: MailService,
) {

    /**
     * Handle payment confirmation event with webhook and email notifications.
     * 
     * Processes PaymentIntentConfirmedEventDto asynchronously to:
     * - Send webhook notification to merchant endpoint
     * - Send email confirmation to merchant
     * 
     * @param event Payment confirmation event with transaction details
     */
    @Async("taskExecutor")
    @EventListener
    fun sendConfirmPaymentIntentEvent(event: PaymentIntentConfirmedEventDto) {
        sendConfirmPaymentIntentWebhook(event)

        mailService.sendConfirmPaymentIntentEmail(
            to = event.merchant.email,
            name = event.merchant.name,
            status = event.paymentIntent.status.name,
            amount = event.paymentIntent.amount,
            currency = event.paymentIntent.currency,
            referenceId = event.paymentIntent.id.toHexString(),
            description = event.paymentIntent.metadata.description ?: "No description provided."
        )
    }

    /**
     * Send webhook notification to merchant endpoint with retry mechanism.
     * 
     * Delivers standardized webhook payload to merchant's configured endpoint
     * with automatic retry on failures and exponential backoff.
     * 
     * @param event Payment confirmation event
     * @throws RuntimeException if webhook delivery fails after all retries
     */
    @Retryable(
        value = [WebClientResponseException::class, SocketTimeoutException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun sendConfirmPaymentIntentWebhook(event: PaymentIntentConfirmedEventDto) {
        val payload = mapOf(
            "eventType" to WebhookEventType.PAYMENT_INTENT_CONFIRMED.name,
            "paymentIntentId" to event.paymentIntent.id.toHexString(),
            "status" to event.paymentIntent.status.name,
            "transactionId" to event.transaction.id.toHexString(),
            "amount" to event.transaction.amount,
            "currency" to event.transaction.currency.name
        )

        webClient.post()
            .uri(event.merchant.webhookUrl!!)
            .bodyValue(payload)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                Mono.error(
                    RuntimeException("Webhook failed: ${response.statusCode()}")
                )
            }
            .bodyToMono(String::class.java)
            .block()
    }
}