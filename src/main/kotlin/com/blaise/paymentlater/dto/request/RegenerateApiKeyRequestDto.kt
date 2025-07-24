package com.blaise.paymentlater.dto.request

import jakarta.validation.constraints.Email

data class RegenerateApiKeyRequestDto(
    @field:Email(message = "Email must be valid")
    val email: String
)
