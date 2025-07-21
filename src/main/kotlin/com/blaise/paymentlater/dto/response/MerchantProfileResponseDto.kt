package com.blaise.paymentlater.dto.response

data class MerchantProfileResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val webhookUrl: String?
)
