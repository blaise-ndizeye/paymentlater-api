package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Schema(description = "Payment intent request details")
data class PaymentIntentRequestDto(
    @field:NotNull(message = "Items cannot be null")
    val items: List<BillableItemRequestDto>,

    @field:NotBlank(message = "Currency is required")
    @field:Pattern(regexp = "RWF|USD|EUR", message = "Invalid currency - It must be RWF, USD or EUR")
    val currency: String,

    @field:NotNull(message = "Metadata cannot be null")
    val metadata: PaymentMetadataRequestDto,
)
