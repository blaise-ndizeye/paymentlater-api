package com.blaise.paymentlater.controller.v1.payments

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

@Schema(description = "Paginated response of PaymentIntent")
private data class PaymentIntentPageResponseDto(
    val content: List<PaymentIntentResponseDto>,
    val totalPages: Int,
    val totalElements: Long,
    val page: Int,
    val size: Int
)

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payments endpoints")
class PaymentControllerV1(
    private val paymentService: PaymentServiceV1
) {

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Create payment intent for a merchant",
        security = [SecurityRequirement(name = "ApiKey")],
        description = "Create payment intent for a merchant",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Payment intent created successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = PaymentIntentResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(type = "object", nullable = true)
                    ),
                ]
            ),
        ]
    )
    fun createPaymentIntent(
        @Parameter(description = "Payment intent request details")
        @Valid @RequestBody body: PaymentIntentRequestDto
    ): PaymentIntentResponseDto = paymentService.createPaymentIntent(body)

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Cancel a payment intent",
        security = [SecurityRequirement(name = "ApiKey")],
        description = "Cancel a payment intent",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Payment intent cancelled successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = PaymentIntentResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(type = "object", nullable = true)
                    ),
                ]
            ),
        ]
    )
    fun cancelPaymentIntent(@PathVariable id: String): PaymentIntentResponseDto = TODO()

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "BearerToken")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Get all payment intents (with filters)",
        security = [SecurityRequirement(name = "BearerToken"), SecurityRequirement(name = "ApiKey")],
        description = "Allows users to fetch all payment intents with optional filters like status, currency, and date range." +
                " For an admin user, all payment intents will be returned.",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = PaymentIntentPageResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(type = "object", nullable = true)
                    ),
                ]
            ),
        ]
    )
    fun getPayments(
        authentication: Authentication,
        @Parameter(description = "List of payment statuses", example = "PENDING, COMPLETED, FAILED, CANCELLED")
        @RequestParam(required = false) statuses: List<String>?,

        @Parameter(description = "List of currencies", example = "RWF, USD, EUR")
        @RequestParam(required = false) currencies: List<String>?,

        @Parameter(description = "Start date in ISO 8601 format", example = "2022-01-01T00:00:00.000Z")
        @RequestParam(required = false) start: String?,

        @Parameter(description = "End date in ISO 8601 format", example = "2022-01-01T00:00:00.000Z")
        @RequestParam(required = false) end: String?,

        @Parameter(description = "Page number", example = "1")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponseDto<PaymentIntentResponseDto> {
        val user = authentication.principal
        val filter = PaymentIntentFilterDto(
            statuses = statuses?.map { PaymentStatus.valueOf(it) },
            currencies = currencies?.map { Currency.valueOf(it) },
            start = start?.let { Instant.parse(it) },
            end = end?.let { Instant.parse(it) },
            merchantId = if (user is Merchant) user.id else null
        )

        return paymentService.getPayments(filter, page, size)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "BearerToken")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Get a payment intent by id",
        security = [SecurityRequirement(name = "BearerToken"), SecurityRequirement(name = "ApiKey")],
        description = "Get a payment intent by id",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = PaymentIntentResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(type = "object", nullable = true)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Payment intent not found", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun getPayment(
        @AuthenticationPrincipal user: Any,
        @Parameter(description = "Payment intent id")
        @PathVariable id: String
    ): PaymentIntentResponseDto = paymentService.getPayment(id, user)
}