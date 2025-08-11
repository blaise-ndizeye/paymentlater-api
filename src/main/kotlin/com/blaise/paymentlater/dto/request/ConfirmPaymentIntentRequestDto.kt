package com.blaise.paymentlater.dto.request

import com.blaise.paymentlater.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Schema(description = "Request details to confirm a payment intent")
data class ConfirmPaymentIntentRequestDto(
    @field:NotNull
    @field:Pattern(regexp = "SUCCESS|FAILED|REFUNDED", message = "status must be SUCCESS, FAILED or REFUNDED")
    val status: String,

    @field:NotNull
    @field:Pattern(
        regexp = "CASH|CARD|MOBILE_MONEY|BANK_TRANSFER|IREMBO_PAY",
        message = "paymentMethod must be CASH, CARD, MOBILE_MONEY, BANK_TRANSFER or IREMBO_PAY"
    )
    val paymentMethod: String,

    @field:Valid
    val metadata: TransactionMetadataRequestDto
)
