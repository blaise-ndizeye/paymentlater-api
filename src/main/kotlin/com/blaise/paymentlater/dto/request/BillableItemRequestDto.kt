package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@Schema(description = "Billable item request details")
data class BillableItemRequestDto(
    @field:NotBlank
    val name: String,

    val description: String,

    @field:Positive
    val unitAmount: BigDecimal,

    @field:Min(1)
    val quantity: Int = 1
)
