package com.blaise.paymentlater.dto.shared

data class RefundHealth(
    val pending: Long,
    val approvedLastWindow: Long,
    val rejectedLastWindow: Long
)
