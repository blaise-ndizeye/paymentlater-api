package com.blaise.paymentlater.dto.response

import java.math.BigDecimal

data class RefundOverviewResponseDto(
    val merchantId: String,
    val merchantName: String,
    val currency: String,
    val count: Long,
    val approved: Long,
    val rejected: Long,
    val approvedRate: Double,
    val totalRefundedAmount: BigDecimal
)
