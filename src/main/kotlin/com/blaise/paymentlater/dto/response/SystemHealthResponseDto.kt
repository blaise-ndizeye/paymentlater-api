package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.dto.shared.MerchantHealth
import com.blaise.paymentlater.dto.shared.RefundHealth
import com.blaise.paymentlater.dto.shared.TransactionHealth

data class SystemHealthResponseDto(
    val windowHours: Long,
    val transactions: TransactionHealth,
    val refunds: RefundHealth,
    val merchants: MerchantHealth
)
