package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.response.TransactionResponseDto

fun Transaction.toTransactionResponseDto(): TransactionResponseDto = TransactionResponseDto(
    id = id.toHexString(),
    paymentIntentId = paymentIntentId.toHexString(),
    parentTransactionId = parentTransactionId?.toHexString(),
    amount = amount,
    currency = currency,
    paymentMethod = paymentMethod,
    status = status,
    metadata = metadata,
    confirmedAt = confirmedAt.toString(),
)