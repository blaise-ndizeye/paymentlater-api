package com.blaise.paymentlater.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MerchantRegisterRequestDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email is required")
    val email: String,

    @field:Size(max = 70, message = "Webhook URL too long")
    val webhookUrl: String? = null
)
