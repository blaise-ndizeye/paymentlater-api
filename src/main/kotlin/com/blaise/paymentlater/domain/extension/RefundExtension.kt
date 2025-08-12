package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto

fun Refund.toRefundResponseDto(): RefundTransactionResponseDto = RefundTransactionResponseDto(
    id = id.toHexString(),
    transactionId = transactionId.toHexString(),
    amount = amount,
    currency = currency,
    reason = reason,
    status = status,
    requestedAt = requestedAt.toString(),
    approvedAt = approvedAt?.toString(),
    rejectedAt = rejectedAt?.toString(),
)