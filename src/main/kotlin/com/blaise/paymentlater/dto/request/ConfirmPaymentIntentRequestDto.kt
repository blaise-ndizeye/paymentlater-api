package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Schema(description = "Request details to confirm a payment intent")
data class ConfirmPaymentIntentRequestDto(
    @field:NotNull
    @field:Pattern(regexp = "SUCCESS|FAILED", message = "status must be SUCCESS or FAILED")
    @field:Schema(hidden = true)
    val status: String = "SUCCESS", // TransactionStatus

    @field:NotNull
    @field:Pattern(
        regexp = "CASH|CARD|MOBILE_MONEY|BANK_TRANSFER",
        message = "paymentMethod must be CASH, CARD, MOBILE_MONEY, BANK_TRANSFER"
    )
    val paymentMethod: String,

    @field:Valid
    val metadata: TransactionMetadataRequestDto
)
