package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.MerchantRegisterResponseDto

fun Merchant.toMerchantRegisterResponseDto(): MerchantRegisterResponseDto = MerchantRegisterResponseDto(
    id = id.toHexString(),
    name = name,
    email = email,
    apiKey = apiKey,
    webhookUrl = webhookUrl,
    createdAt = createdAt.toString(),
)

fun Merchant.toMerchantProfileResponseDto(): MerchantProfileResponseDto =
    MerchantProfileResponseDto(
        id = id.toHexString(),
        name = name,
        email = email,
        webhookUrl = webhookUrl,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )