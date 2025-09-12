package com.blaise.paymentlater.dto.response

import java.math.BigDecimal

data class TransactionOverviewResponseDto(
    val merchantId: String,
    val merchantName: String,
    val currency: String,
    val count: Long,
    val totalAmount: BigDecimal,
    val succeeded: Long,
    val successRate: Double,
    val avgOrderValue: BigDecimal
)
