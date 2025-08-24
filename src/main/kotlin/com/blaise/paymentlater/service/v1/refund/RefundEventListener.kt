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

@Component
class RefundEventListener(
    private val mailService: MailService,
    private val webClient: WebClient
) {

    @Async("taskExecutor")
    @EventListener
    fun sendRefundUpdateEvent(event: RefundUpdateEventDto) {
        if (event.refund.status == RefundStatus.APPROVED) {
            sendRefundApprovedEventWebhook(WebhookEventType.REFUND_APPROVED, event)
            mailService.sendRefundApprovedEmail(
                to = event.merchant.email,
                name = event.merchant.name,
                amount = event.refund.amount,
                currency = event.refund.currency,
                reason = event.refund.reason
            )
        } else if (event.refund.status == RefundStatus.REJECTED) {
            sendRefundApprovedEventWebhook(WebhookEventType.REFUND_REJECTED, event)
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

    @Retryable(
        value = [WebClientResponseException::class, SocketTimeoutException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0)
    )
    fun sendRefundApprovedEventWebhook(eventType: WebhookEventType, event: RefundUpdateEventDto) {
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