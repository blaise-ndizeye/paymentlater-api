package com.blaise.paymentlater.dto.shared

data class TransactionHealth(
    val total: Long,
    val byStatus: Map<String, Long>
)