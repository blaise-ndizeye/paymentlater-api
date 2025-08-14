package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Reject refund request details")
data class RejectRefundRequestDto(

    @field:NotBlank(message = "Reason is required")
    val reason: String
)
