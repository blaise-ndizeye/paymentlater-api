package com.blaise.paymentlater.dto.response

data class MerchantRegisterResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val apiKey: String,
    val webhookUrl: String?,
    val createdAt: String
)