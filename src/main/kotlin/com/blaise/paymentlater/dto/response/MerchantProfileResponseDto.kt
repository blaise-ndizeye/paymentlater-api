package com.blaise.paymentlater.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Merchant profile response details")
data class MerchantProfileResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val webhookUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
