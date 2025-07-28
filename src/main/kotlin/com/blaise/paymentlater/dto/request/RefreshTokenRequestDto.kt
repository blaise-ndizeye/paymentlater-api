package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Refresh token request details")
data class RefreshTokenRequestDto(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)
