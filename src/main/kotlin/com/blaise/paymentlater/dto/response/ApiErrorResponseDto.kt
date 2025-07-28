package com.blaise.paymentlater.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Standard error response format")
data class ApiErrorResponseDto(
    @Schema(description = "Error message")
    val message: String,

    @Schema(description = "Timestamp of the error")
    val timestamp: Instant = Instant.now(),
)
