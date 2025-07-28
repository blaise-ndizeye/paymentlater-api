package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email

@Schema(description = "Regenerate API key request details")
data class RegenerateApiKeyRequestDto(
    @field:Email(message = "Email must be valid")
    val email: String
)
