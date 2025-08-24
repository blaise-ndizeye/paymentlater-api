package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.enum.PaymentStatus
import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toRefundResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import com.blaise.paymentlater.dto.shared.RefundUpdateEventDto
import com.blaise.paymentlater.repository.RefundRepository
import com.blaise.paymentlater.service.v1.admin.AdminAuthServiceV1
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
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
class RefundServiceV1Impl(
    private val refundRepository: RefundRepository,
    private val paymentService: PaymentServiceV1,
    private val transactionService: TransactionServiceV1,
    private val adminAuthService: AdminAuthServiceV1,
    private val merchantAuthService: MerchantAuthServiceV1,
    private val eventPublisher: ApplicationEventPublisher
) : RefundServiceV1 {

    @Transactional
    override fun approveRefund(refundId: String): RefundTransactionResponseDto {
        val refund = findById(refundId)

        if (refund.status != RefundStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refund is not pending")
        }

        val (transaction, paymentIntent) = transactionService.getTransactionAndAssociatedPaymentIntent(
            refund.transactionId.toHexString()
        )

        val admin = adminAuthService.getAuthenticatedAdmin()
        val associatedMerchant = merchantAuthService.findById(paymentIntent.merchantId) /*
        * Use this instead of merchantService.getAuthenticatedMerchant()
        * because authenticated user is an admin only
        */

        val totalRefundedSoFar = refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id)
        val totalAfterThisRefund = totalRefundedSoFar + refund.amount

        val newPaymentIntentStatus = when {
            totalAfterThisRefund == paymentIntent.amount -> PaymentStatus.REFUNDED
            totalAfterThisRefund < paymentIntent.amount -> PaymentStatus.PARTIALLY_REFUNDED
            else -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Refund amount exceeds payment intent total"
            )
        }

        val updatedPaymentIntent = paymentService.save(
            paymentIntent.copy(status = newPaymentIntentStatus)
        )

        val newTransaction = transactionService.save(
            Transaction(
                paymentIntentId = updatedPaymentIntent.id,
                parentTransactionId = transaction.id,
                amount = refund.amount,
                currency = refund.currency,
                status = TransactionStatus.REFUNDED,
                paymentMethod = transaction.paymentMethod,
                metadata = transaction.metadata.copy(refundReason = refund.reason),
            )
        )

        val updatedRefund = refundRepository.save(
            refund.copy(
                status = RefundStatus.APPROVED,
                approvedBy = admin.id.toHexString(),
                approvedAt = Instant.now()
            )
        )

        eventPublisher.publishEvent(
            RefundUpdateEventDto(
                updatedRefund,
                updatedPaymentIntent,
                newTransaction,
                associatedMerchant
            )
        )

        return updatedRefund
            .toRefundResponseDto()
            .also {
                log.info { "Approved refund with id $refundId" }
            }
    }

    override fun rejectRefund(refundId: String, body: RejectRefundRequestDto): RefundTransactionResponseDto {
        val refund = findById(refundId)

        if (refund.status != RefundStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refund is not pending")
        }

        val (transaction, paymentIntent) = transactionService.getTransactionAndAssociatedPaymentIntent(
            refund.transactionId.toHexString()
        )

        val admin = adminAuthService.getAuthenticatedAdmin()
        val associatedMerchant = merchantAuthService.findById(paymentIntent.merchantId)

        val updatedRefund = refundRepository.save(
            refund.copy(
                rejectedReason = body.reason,
                status = RefundStatus.REJECTED,
                rejectedBy = admin.id.toHexString(),
                rejectedAt = Instant.now()
            )
        )

        eventPublisher.publishEvent(
            RefundUpdateEventDto(
                updatedRefund,
                paymentIntent,
                transaction,
                associatedMerchant
            )
        )

        return updatedRefund
            .toRefundResponseDto()
            .also {
                log.info { "Rejected refund with id $refundId" }
            }
    }

    override fun getRefund(refundId: String, user: Any): RefundTransactionResponseDto {
        val refund = findById(refundId)
        val (_, paymentIntent) = transactionService.getTransactionAndAssociatedPaymentIntent(
            refund.transactionId.toHexString()
        )

        if (user is Merchant && user.id != paymentIntent.merchantId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        return refund.toRefundResponseDto()
    }

    override fun getRefunds(
        filter: RefundFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<RefundTransactionResponseDto> {
        val refunds = refundRepository.search(filter, page, size)

        return refunds.map { it.toRefundResponseDto() }
            .toPageResponseDto()
            .also {
                log.info { "Found ${refunds.totalElements} refunds" }
            }
    }

    override fun findById(refundId: String): Refund = refundRepository.findById(ObjectId(refundId))
        .orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Refund with id $refundId not found")
        }
}