package com.blaise.paymentlater.controller.v1.refund

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import com.blaise.paymentlater.service.v1.refund.RefundServiceV1
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/refunds")
@Tag(name = "Refunds", description = "Refund endpoints")
class RefundControllerV1(
    private val refundService: RefundServiceV1
) {

    @PatchMapping("/{refundId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Approve refund",
        description = "Approve refund",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Refund approved successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefundTransactionResponseDto::class)
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
            ApiResponse(
                responseCode = "404",
                description = "Refund not found",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            )
        ]
    )
    fun approveRefund(
        @Parameter(description = "Refund id")
        @PathVariable refundId: String
    ): RefundTransactionResponseDto = refundService.approveRefund(refundId)

    @PatchMapping("/{refundId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Reject refund",
        description = "Reject refund",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Refund rejected successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefundTransactionResponseDto::class)
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
            ApiResponse(
                responseCode = "404",
                description = "Refund not found",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            )
        ]
    )
    fun rejectRefund(
        @Parameter(description = "Refund id")
        @PathVariable refundId: String,

        @Valid @RequestBody body: RejectRefundRequestDto
    ): RefundTransactionResponseDto = refundService.rejectRefund(refundId, body)

    @GetMapping("/{refundId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "BearerToken")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Get a refund by id",
        security = [SecurityRequirement(name = "BearerToken"), SecurityRequirement(name = "ApiKey")],
        description = "Get a refund by id",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefundTransactionResponseDto::class)
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
                responseCode = "403", description = "Forbidden", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(type = "object", nullable = true)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Refund not found", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun getRefund(
        authentication: Authentication,
        @Parameter(description = "Refund id")
        @PathVariable refundId: String
    ): RefundTransactionResponseDto = refundService.getRefund(refundId, authentication.principal)

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "BearerToken")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Get all refunds",
        security = [SecurityRequirement(name = "BearerToken"), SecurityRequirement(name = "ApiKey")],
        description = "Get all refunds",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefundTransactionResponseDto::class)
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
            )
        ]
    )
    fun getAllRefunds(
        authentication: Authentication,

        @Parameter(description = "List of refund statuses", example = "PENDING, APPROVED, REJECTED")
        @RequestParam(required = false) statuses: List<String>?,

        @Parameter(description = "List of currencies", example = "RWF, USD, EUR")
        @RequestParam(required = false) currencies: List<String>?,

        @Parameter(description = "Approved date start in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) approvedDateStart: String?,

        @Parameter(description = "Approved date end in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) approvedDateEnd: String?,

        @Parameter(description = "Rejected date start in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) rejectedDateStart: String?,

        @Parameter(description = "Rejected date end in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) rejectedDateEnd: String?,

        @Parameter(description = "Page number", example = "1")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponseDto<RefundTransactionResponseDto> {
        val user = authentication.principal
        val filter = RefundFilterDto(
            statuses = statuses?.map { RefundStatus.valueOf(it) },
            currencies = currencies?.map { Currency.valueOf(it) },
            approvedDateStart = approvedDateStart?.let { Instant.parse(it) },
            approvedDateEnd = approvedDateEnd?.let { Instant.parse(it) },
            rejectedDateStart = rejectedDateStart?.let { Instant.parse(it) },
            rejectedDateEnd = rejectedDateEnd?.let { Instant.parse(it) },
            merchantId = if (user is Merchant) user.id else null
        )

        return refundService.getRefunds(filter, page, size)
    }
}