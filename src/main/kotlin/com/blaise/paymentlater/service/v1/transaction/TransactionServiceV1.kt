package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.model.Transaction

interface TransactionServiceV1 {
    fun save(transaction: Transaction): Transaction
}