package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.response.MerchantResponseDto

fun Merchant.toMerchantResponseDto(): MerchantResponseDto = MerchantResponseDto(
    id = id.toHexString(),
    name = name,
    email = email,
    apiKey = apiKey,
    webhookUrl = webhookUrl,
    createdAt = createdAt.toString(),
)