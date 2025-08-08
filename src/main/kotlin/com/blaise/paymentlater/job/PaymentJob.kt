package com.blaise.paymentlater.job

import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

private val log = KotlinLogging.logger {}
@Component
class PaymentJob(
    private val paymentService: PaymentServiceV1
) {

    @PostConstruct
    fun init() {
        log.info { "JOB: Payment job initialized" }
    }

    @Scheduled(cron = "0 */5 * * * *")
    fun expireOldPaymentIntents() {
        log.info { "JOB: Checking for expired payment intents" }
        try {
            paymentService.expireOldPaymentIntents(Instant.now())
        } catch (e: Exception) {
            log.error("Failed to expire payment intents", e)
        }
    }
}