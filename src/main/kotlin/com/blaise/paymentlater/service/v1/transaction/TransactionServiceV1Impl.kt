package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.enums.RefundStatus
import com.blaise.paymentlater.domain.enums.TransactionStatus
import com.blaise.paymentlater.domain.extension.toRefundResponseDto
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
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

        if (body.amount > transaction.amount) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Refund amount cannot be greater than transaction amount"
        )

        if (paymentIntent.status != PaymentStatus.COMPLETED) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Payment intent is not completed"
        )

        if (transaction.status != TransactionStatus.SUCCESS) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Transaction is not successful"
        )

        val newRefund = refundRepository.save(
            Refund(
                transactionId = transaction.id,
                status = RefundStatus.PENDING,
                reason = body.reason,
                amount = body.amount,
                currency = transaction.currency
            )
        )

        return newRefund
            .toRefundResponseDto()
            .also {
                log.info { "Created refund: ${it.id}" }
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
}