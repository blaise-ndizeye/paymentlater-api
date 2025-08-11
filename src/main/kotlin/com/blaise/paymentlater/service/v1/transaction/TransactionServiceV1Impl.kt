package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class TransactionServiceV1Impl(
    private val transactionRepository: TransactionRepository
) : TransactionServiceV1 {

    override fun save(transaction: Transaction): Transaction {
        return transactionRepository.save(transaction)
    }
}