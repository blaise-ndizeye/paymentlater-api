package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentMethod
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.enums.TransactionStatus
import com.blaise.paymentlater.domain.enums.UserRole
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toPaymentIntentResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.ConfirmPaymentIntentRequestDto
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentConfirmedEventDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import com.blaise.paymentlater.repository.PaymentIntentRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import com.blaise.paymentlater.service.v1.transaction.TransactionServiceV1
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

private val log = KotlinLogging.logger {}

@Service
class PaymentServiceV1Impl(
    private val paymentIntentRepository: PaymentIntentRepository,
    private val merchantAuthService: MerchantAuthServiceV1,
    private val transactionService: TransactionServiceV1,
    private val eventPublisher: ApplicationEventPublisher,
) : PaymentServiceV1 {

    override fun getPayments(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto> {
        val paymentIntents = paymentIntentRepository.search(filter, page, size)

        return paymentIntents.map { it.toPaymentIntentResponseDto() }
            .toPageResponseDto()
            .also {
                log.info { "Found ${paymentIntents.totalElements} payment intents" }
            }
    }

    override fun getPayment(id: String, user: Any): PaymentIntentResponseDto {
        val paymentIntent = findById(id)
        paymentIntent.let {
            log.info { "Found payment intent: ${paymentIntent.id}" }
        }

        return when (user) {
            is Merchant -> {
                if (user.id != paymentIntent.merchantId)
                    throw ResponseStatusException(HttpStatus.FORBIDDEN)
                paymentIntent.toPaymentIntentResponseDto()
            }

            is Admin -> paymentIntent.toPaymentIntentResponseDto()
            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    override fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto {
        val merchant = merchantAuthService.getAuthenticatedMerchant()
        val totalAmount = body.items.sumOf { it.unitAmount * it.quantity.toBigDecimal() }

        return paymentIntentRepository.save(
            PaymentIntent(
                merchantId = merchant.id,
                items = body.items,
                amount = totalAmount,
                currency = Currency.valueOf(body.currency),
                metadata = body.metadata,
            )
        )
            .toPaymentIntentResponseDto()
            .also { log.info { "Created payment intent: ${it.id}" } }
    }

    override fun expireOldPaymentIntents(now: Instant) {
        val expiredIntents = paymentIntentRepository.findPendingWithExpiredAtBefore(now)
        expiredIntents.forEach {
            log.info { "Expired payment intent: ${it.id}" }
            val updated = it.copy(status = PaymentStatus.EXPIRED)
            paymentIntentRepository.save(updated)
        }
    }

    override fun cancelPaymentIntent(paymentIntentId: String, user: Any): PaymentIntentResponseDto {
        val paymentIntent = findById(paymentIntentId)

        if (paymentIntent.status != PaymentStatus.PENDING)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent is not pending")

        val canceller = when (user) {
            is Admin -> user.id
            is Merchant -> {
                if (user.id != paymentIntent.merchantId)
                    throw ResponseStatusException(HttpStatus.FORBIDDEN)
                user.id
            }

            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        /*
         * Possible when the payment intent is not yet marked as EXPIRED
         * by the PaymentJob (see PaymentJob.expireOldPaymentIntents)
         */
        if (Instant.now().isAfter(paymentIntent.expiresAt))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent is expired")

        val updated = paymentIntent.copy(
            status = PaymentStatus.CANCELLED,
            cancelledAt = Instant.now(),
            cancelledBy = canceller
        )

        return paymentIntentRepository.save(updated).toPaymentIntentResponseDto()
    }

    @Transactional
    override fun confirmPaymentIntent(
        paymentIntentId: String,
        body: ConfirmPaymentIntentRequestDto
    ): PaymentIntentResponseDto {
        validateConfirmPaymentRequest(body)

        val paymentIntent = findById(paymentIntentId)
        val merchant = merchantAuthService.getAuthenticatedMerchant()

        ensureConfirmPaymentIntentAuthorization(paymentIntent, merchant)

        val updated = paymentIntent.copy(
            status = mapStatus(TransactionStatus.valueOf(body.status)),
        )

        val transaction = transactionService.save(
            Transaction(
                paymentIntentId = updated.id,
                paymentMethod = PaymentMethod.valueOf(body.paymentMethod),
                amount = updated.amount,
                currency = updated.currency,
                status = TransactionStatus.valueOf(body.status),
                confirmedBy = merchant.id,
                confirmedByRole = UserRole.MERCHANT,
                metadata = body.metadata
            )
        )

        val updatedPaymentIntent = paymentIntentRepository.save(updated)

        eventPublisher.publishEvent(
            PaymentIntentConfirmedEventDto(
                merchant = merchant,
                paymentIntent = updatedPaymentIntent,
                transaction = transaction
            )
        )

        return updatedPaymentIntent.toPaymentIntentResponseDto()
    }

    override fun findById(id: String): PaymentIntent {
        return paymentIntentRepository.findById(ObjectId(id)).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Payment intent with id $id not found")
        }
    }

    private fun ensureConfirmPaymentIntentAuthorization(
        paymentIntent: PaymentIntent,
        merchant: Merchant
    ) {
        if (paymentIntent.merchantId != merchant.id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        if (merchant.webhookUrl.isNullOrBlank())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Set webhook url first and try again")

        if (paymentIntent.status != PaymentStatus.PENDING)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent is not pending")

        if (Instant.now().isAfter(paymentIntent.expiresAt))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent is expired")
    }

    private fun mapStatus(status: TransactionStatus) = when (status) {
        TransactionStatus.FAILED -> PaymentStatus.FAILED
        TransactionStatus.REFUNDED -> PaymentStatus.COMPLETED
        else -> PaymentStatus.COMPLETED
    }

    private fun validateConfirmPaymentRequest(body: ConfirmPaymentIntentRequestDto) {
        when (TransactionStatus.valueOf(body.status)) {
            TransactionStatus.FAILED -> {
                requireNotNull(body.metadata.failureReason) { "failureReason is required when status is FAILED" }
            }

            TransactionStatus.REFUNDED -> {
                requireNotNull(body.metadata.refundReason) { "refundReason is required when status is REFUNDED" }
            }

            else -> {}
        }
    }
}