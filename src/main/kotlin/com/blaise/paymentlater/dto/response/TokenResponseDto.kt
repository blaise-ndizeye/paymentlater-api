package com.blaise.paymentlater.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Token response details")
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)
