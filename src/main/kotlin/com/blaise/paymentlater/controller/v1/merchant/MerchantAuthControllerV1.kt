package com.blaise.paymentlater.controller.v1.merchant

import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.request.RegenerateApiKeyRequestDto
import com.blaise.paymentlater.dto.request.SetWebhookRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.MerchantRegisterResponseDto
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/merchant/auth")
@Tag(name = "Merchant Auth", description = "Merchant authentication endpoints")
class MerchantAuthControllerV1(
    private val merchantAuthService: MerchantAuthServiceV1
) {

    @PostMapping("/register")
    @Operation(
        summary = "Register a new merchant",
        description = "Register a new merchant user",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MerchantRegisterResponseDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "User already exists", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun register(
        @Valid @RequestBody
        @Parameter(description = "Merchant registration details")
        merchant: MerchantRegisterRequestDto
    ): MerchantRegisterResponseDto = merchantAuthService.register(merchant)

    @PostMapping("/regenerate-api-key")
    @Operation(
        summary = "Regenerate API key for a merchant",
        description = "Regenerate API key for a merchant",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Unit::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Merchant not found", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "500", description = "Email sending failed", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun regenerateApiKey(
        @Valid @RequestBody
        @Parameter(description = "Merchant regenerate API key details")
        body: RegenerateApiKeyRequestDto
    ): ResponseEntity<Unit> =
        merchantAuthService.regenerateApiKey(body.email)


    @PostMapping("/set-webhook")
    @PreAuthorize("hasRole('MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Set webhook for a merchant",
        description = "Set webhook for a merchant",
        security = [SecurityRequirement(name = "ApiKey")],
        responses = [
            ApiResponse(responseCode = "200", description = "Webhook set successfully"),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Unit::class)
                    ),
                ]
            ),
        ]
    )
    fun setWebhook(@Valid @RequestBody body: SetWebhookRequestDto): ResponseEntity<Unit> =
        merchantAuthService.setWebhook(body.webhookUrl)

    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Get merchant profile",
        description = "Get merchant profile",
        security = [SecurityRequirement(name = "ApiKey")],
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MerchantProfileResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Unit::class)
                    ),
                ]
            ),
        ]
    )
    fun me(@AuthenticationPrincipal merchant: Merchant): MerchantProfileResponseDto =
        merchant.toMerchantProfileResponseDto()
}