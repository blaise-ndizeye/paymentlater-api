package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "Refund transaction request details")
data class RefundTransactionRequestDto(
    @field:NotNull(message = "Amount is required")
    @field:Min(value = 1, message = "Amount must be greater than 0")
    val amount: BigDecimal,

    @field:NotBlank(message = "Reason is required")
    val reason: String,
)