package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.service.v1.admin.AdminPaymentServiceV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
@RequestMapping("/api/v1/admin/payments")
@Tag(name = "Admin Payments", description = "Admin payments endpoints")
class AdminPaymentControllerV1(
    private val adminPaymentService: AdminPaymentServiceV1
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get all payment intents (with filters)",
        security = [SecurityRequirement(name = "BearerToken")],
        description = "Allows admin to fetch all payment intents with optional filters like status, currency, and date range.",
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
    ): PageResponseDto<PaymentIntentResponseDto> =
        adminPaymentService.search(
            statuses = statuses?.map { PaymentStatus.valueOf(it) },
            currencies = currencies?.map { Currency.valueOf(it) },
            start = start?.let { Instant.parse(it) },
            end = end?.let { Instant.parse(it) },
            page,
            size
        )
}