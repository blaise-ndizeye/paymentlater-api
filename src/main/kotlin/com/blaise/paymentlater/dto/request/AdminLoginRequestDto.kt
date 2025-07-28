package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Admin login request details")
data class AdminLoginRequestDto(
    @Schema(description = "Admin username")
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,

    @Schema(description = "Admin password")
    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)
