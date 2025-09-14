package com.blaise.paymentlater.controller.v1.welcome

import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
@Tag(name = "Welcome", description = "Entry endpoint")
class WelcomeController {

    @GetMapping
    @Operation(
        summary = "PaymentLater API",
        description = "PaymentLater API",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = String::class)
                    ),
                ]
            )
        ]
    )
    fun welcome(): String = "Welcome to PaymentLater API"
}