package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Schema(description = "Payment metadata request details")
data class PaymentMetadataRequestDto(
    @field:NotBlank
    val referenceId: String,

    val userId: String?,

    @field:Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "Invalid phone number format"
    )
    val phone: String?,

    @field:Email
    val email: String?,

    val description: String?
)
