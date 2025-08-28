package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@Schema(description = "Update merchant request details")
data class UpdateMerchantRequestDto(
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val name: String? = null,

    @field:Email(message = "Email must be valid")
    val email: String? = null,

    @field:URL(message = "Webhook URL must be valid")
    val webhookUrl: String? = null,

    @field:Schema(description = "Roles for the merchant", example = "[\"MERCHANT\"]")
    val roles: List<String>? = null,
)
