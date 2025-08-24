package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.enum.PaymentStatus
import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toRefundResponseDto
import com.blaise.paymentlater.domain.extension.toTransactionResponseDto
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.response.TransactionResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import com.blaise.paymentlater.repository.RefundRepository
import com.blaise.paymentlater.repository.TransactionRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

private val log = KotlinLogging.logger {}

@Service
class TransactionServiceV1Impl(
    private val transactionRepository: TransactionRepository,
    private val refundRepository: RefundRepository,
    private val paymentService: PaymentServiceV1,
    private val merchantAuthService: MerchantAuthServiceV1
) : TransactionServiceV1 {

    override fun save(transaction: Transaction): Transaction {
        return transactionRepository.save(transaction)
    }

    @Transactional
    override fun requestRefundTransaction(
        transactionId: String,
        body: RefundTransactionRequestDto
    ): RefundTransactionResponseDto {
        val (transaction, paymentIntent) = getTransactionAndAssociatedPaymentIntent(transactionId)
        val merchant = merchantAuthService.getAuthenticatedMerchant()

        if (merchant.id != paymentIntent.merchantId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        if (paymentIntent.status !in listOf(PaymentStatus.COMPLETED, PaymentStatus.PARTIALLY_REFUNDED))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent is not eligible for refund")

        if (transaction.status in listOf(TransactionStatus.FAILED, TransactionStatus.REFUNDED))
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction is not eligible for refund")

        val totalRefundedSoFar = refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id)
        val remainingRefundable = paymentIntent.amount - totalRefundedSoFar

        if (body.amount > remainingRefundable)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Refund amount exceeds remaining refundable balance")

        val newRefund = refundRepository.save(
            Refund(
                transactionId = transaction.id,
                status = RefundStatus.PENDING,
                reason = body.reason,
                amount = body.amount,
                currency = transaction.currency,
            )
        )

        return newRefund
            .toRefundResponseDto()
            .also {
                log.info { "Created a new refund: ${it.id}" }
            }
    }

    override fun findById(id: ObjectId): Transaction = transactionRepository.findById(id).orElseThrow {
        ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with id $id not found")
    }

    override fun getTransactionAndAssociatedPaymentIntent(transactionId: String): Pair<Transaction, PaymentIntent> {
        val transaction = findById(ObjectId(transactionId))
        val paymentIntent = paymentService.findById(transaction.paymentIntentId.toHexString())
        return Pair(transaction, paymentIntent)
    }

    override fun getTransactions(
        filter: TransactionFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<TransactionResponseDto> {
        val transactions = transactionRepository.search(filter, page, size)

        return transactions.map { it.toTransactionResponseDto() }
            .toPageResponseDto()
            .also {
                log.info { "Found ${transactions.totalElements} transactions" }
            }
    }
}