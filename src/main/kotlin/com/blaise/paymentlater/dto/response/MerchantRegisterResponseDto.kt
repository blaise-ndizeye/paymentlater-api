package com.blaise.paymentlater.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Merchant registration response details")
data class MerchantRegisterResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val apiKey: String,
    val webhookUrl: String?,
    val createdAt: String
)