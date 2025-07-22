package com.blaise.paymentlater.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size


data class AdminRegisterRequestDto(
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,

    @field:Pattern(
        regexp = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@#%*+=?<>|{}\[\]"~:;,./()\-_]).{6,32}$""",
        message = "Invalid password"
    ) // Password must be 6-32 long, contain at least one lowercase letter, one uppercase letter, one digit, and one special character.
    val password: String
)
