package com.blaise.paymentlater.dto.request

import jakarta.validation.constraints.NotBlank

data class AdminLoginRequestDto(
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,

    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)
