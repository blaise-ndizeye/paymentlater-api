package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@Schema(description = "Merchant registration request details")
data class MerchantRegisterRequestDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val name: String,


    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email is required")
    val email: String,

    @field:URL(message = "Webhook URL must be valid")
    val webhookUrl: String? = null
)
