package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.RefundStatus
import java.math.BigDecimal

data class RefundTransactionResponseDto(
    val id: String,

    val transactionId: String,

    val amount: BigDecimal,

    val currency: Currency,

    val reason: String,

    val status: RefundStatus,

    val requestedAt: String,

    val approvedAt: String? = null,

    val rejectedAt: String? = null,
)
