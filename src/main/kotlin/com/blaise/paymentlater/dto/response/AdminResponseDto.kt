package com.blaise.paymentlater.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Admin response details")
data class AdminResponseDto(
    val id: String,
    val username: String,
    val createdAt: String
)
