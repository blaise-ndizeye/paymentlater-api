package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto

fun PaymentIntent.toPaymentIntentResponseDto(): PaymentIntentResponseDto = PaymentIntentResponseDto(
    id = id.toHexString(),
    amount = amount,
    currency = currency,
    status = status,
    metadata = metadata,
    createdAt = createdAt.toString(),
    expiresAt = expiresAt.toString(),
    merchantId = merchantId.toHexString(),
    items = items
)