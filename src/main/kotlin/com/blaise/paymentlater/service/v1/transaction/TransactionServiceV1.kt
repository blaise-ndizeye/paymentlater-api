package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.response.TransactionResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import org.bson.types.ObjectId

interface TransactionServiceV1 {
    fun save(transaction: Transaction): Transaction

    fun requestRefundTransaction(transactionId: String, body: RefundTransactionRequestDto): RefundTransactionResponseDto

    fun findById(id: ObjectId): Transaction

    fun getTransactionAndAssociatedPaymentIntent(transactionId: String): Pair<Transaction, PaymentIntent>

    fun getTransactions(filter: TransactionFilterDto, page: Int, size: Int): PageResponseDto<TransactionResponseDto>
}