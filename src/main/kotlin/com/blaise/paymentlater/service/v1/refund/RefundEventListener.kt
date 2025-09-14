package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.enum.WebhookEventType
import com.blaise.paymentlater.dto.shared.RefundUpdateEventDto
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
 * Event listener for refund status updates with webhook and email notifications.
 * 
 * Handles refund lifecycle events for approved and rejected refunds:
 * 
 * **Event Processing**:
 * - Refund approval/rejection notifications
 * - Conditional webhook payload generation
 * - Status-specific email delivery
 * - Asynchronous processing for performance
 * 
 * **Notification Types**:
 * - REFUND_APPROVED: Success notifications with approval timestamps
 * - REFUND_REJECTED: Rejection notifications with admin reasoning
 * - Dynamic payload generation based on refund status
 * 
 * **Reliability Features**:
 * - Automatic webhook retry with exponential backoff
 * - Comprehensive error handling for failed deliveries
 * - Non-blocking merchant notifications
 */
@Component
class RefundEventListener(
    private val mailService: MailService,
    private val webClient: WebClient
) {

    /**
     * Handle refund status update events with appropriate notifications.
     * 
     * Routes refund events to appropriate notification channels based on status:
     * - APPROVED: Sends approval webhook and email
     * - REJECTED: Sends rejection webhook and email with reasoning
     * 
     * @param event Refund update event containing refund and merchant details
     */
    @Async("taskExecutor")
    @EventListener
    fun sendRefundUpdateEvent(event: RefundUpdateEventDto) {
        if (event.refund.status == RefundStatus.APPROVED) {
            sendRefundEventWebhook(WebhookEventType.REFUND_APPROVED, event)
            mailService.sendRefundApprovedEmail(
                to = event.merchant.email,
                name = event.merchant.name,
                amount = event.refund.amount,
                currency = event.refund.currency,
                reason = event.refund.reason
            )
        } else if (event.refund.status == RefundStatus.REJECTED) {
            sendRefundEventWebhook(WebhookEventType.REFUND_REJECTED, event)
            mailService.sendRefundRejectedEmail(
                to = event.merchant.email,
                name = event.merchant.name,
                amount = event.refund.amount,
                currency = event.refund.currency,
                reason = event.refund.reason,
                rejectedReason = event.refund.rejectedReason!!
            )
        }

    }

    /**
     * Send refund webhook notification with status-specific payload.
     * 
     * Constructs dynamic webhook payload based on refund status and
     * delivers to merchant endpoint with a retry mechanism.
     * 
     * @param eventType Webhook event type (REFUND_APPROVED/REFUND_REJECTED)
     * @param event Refund update event
     * @throws RuntimeException if webhook delivery fails after all retries
     */
    @Retryable(
        value = [WebClientResponseException::class, SocketTimeoutException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun sendRefundEventWebhook(eventType: WebhookEventType, event: RefundUpdateEventDto) {
        val payload = mutableMapOf(
            "eventType" to eventType.name,
            "paymentIntentId" to event.paymentIntent.id.toHexString(),
            "transactionId" to event.transaction.id.toHexString(),
            "refundId" to event.refund.id.toHexString(),
            "amount" to event.refund.amount,
            "currency" to event.refund.currency.name,
            "status" to event.refund.status.name,
            "reason" to event.refund.reason
        )

        if (event.refund.status == RefundStatus.APPROVED) {
            payload["approvedAt"] = event.refund.approvedAt.toString()
        }

        if (event.refund.status == RefundStatus.REJECTED && event.refund.rejectedReason != null) {
            payload["rejectedReason"] = event.refund.rejectedReason
            payload["rejectedAt"] = event.refund.rejectedAt.toString()
        }

        webClient.post()
            .uri(event.merchant.webhookUrl!!)
            .bodyValue(payload)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                Mono.error(
                    RuntimeException("Webhook failed: ${response.statusCode()}")
                )
            }
            .toBodilessEntity()
            .block()
    }
}