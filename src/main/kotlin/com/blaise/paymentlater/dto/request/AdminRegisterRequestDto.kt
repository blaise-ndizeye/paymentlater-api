package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Admin registration request details")
data class AdminRegisterRequestDto(
    @Schema(description = "Admin username")
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,

    @Schema(description = "Admin password - Password must be 6-32 long, contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
    @field:Pattern(
        regexp = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@#%*+=?<>|{}\[\]"~:;,./()\-_]).{6,32}$""",
        message = "Invalid password"
    )
    val password: String
)
