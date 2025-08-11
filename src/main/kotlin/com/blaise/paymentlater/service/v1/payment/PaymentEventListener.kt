package com.blaise.paymentlater.service.v1.payment

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

@Component
class PaymentEventListener(
    private val webClient: WebClient,
    private val mailService: MailService,
) {

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

    @Retryable(
        value = [WebClientResponseException::class, SocketTimeoutException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun sendConfirmPaymentIntentWebhook(event: PaymentIntentConfirmedEventDto) {
        val payload = mapOf(
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