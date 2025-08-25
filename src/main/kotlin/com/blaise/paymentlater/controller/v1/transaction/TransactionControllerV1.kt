package com.blaise.paymentlater.controller.v1.transaction

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.response.TransactionResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import com.blaise.paymentlater.service.v1.transaction.TransactionServiceV1
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

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transaction endpoints")
class TransactionControllerV1(
    private val transactionService: TransactionServiceV1
) {

    @PatchMapping("/{transactionId}/refund")
    @PreAuthorize("hasRole('MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @Operation(
        summary = "Request to refund a transaction by merchant",
        security = [SecurityRequirement(name = "ApiKey")],
        description = "Request to refund a transaction by merchant",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transaction refunded successfully",
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
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Transaction not found",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    )
                ]
            )
        ]
    )
    fun requestRefundTransaction(
        @Parameter(description = "Transaction id")
        @PathVariable transactionId: String,

        @Valid @RequestBody body: RefundTransactionRequestDto
    ): RefundTransactionResponseDto = transactionService.requestRefundTransaction(transactionId, body)

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get all transactions",
        security = [SecurityRequirement(name = "ApiKey"), SecurityRequirement(name = "BearerToken")],
        description = "Get all transactions",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TransactionPageResponseDto::class)
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
    fun getAllTransactions(
        authentication: Authentication,
        @Parameter(description = "List of Transaction statuses", example = "PENDING, COMPLETED, FAILED, CANCELLED")
        @RequestParam(required = false) statuses: List<String>?,

        @Parameter(description = "List of currencies", example = "RWF, USD, EUR")
        @RequestParam(required = false) currencies: List<String>?,

        @Parameter(description = "Start date in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) start: String?,

        @Parameter(description = "End date in ISO 8601 format", example = "2025-01-01T00:00:00.000Z")
        @RequestParam(required = false) end: String?,

        @Parameter(description = "Page number", example = "1")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponseDto<TransactionResponseDto> {
        val user = authentication.principal
        val filter = TransactionFilterDto(
            statuses = statuses?.map { TransactionStatus.valueOf(it) },
            currencies = currencies?.map { Currency.valueOf(it) },
            start = start?.let { Instant.parse(it) },
            end = end?.let { Instant.parse(it) },
            merchantId = if (user is Merchant) user.id else null
        )

        return transactionService.getTransactions(filter, page, size)
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    @SecurityRequirement(name = "ApiKey")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get a transaction by id",
        security = [SecurityRequirement(name = "ApiKey"), SecurityRequirement(name = "BearerToken")],
        description = "Get a transaction by id",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TransactionPageResponseDto::class)
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
    fun getTransaction(
        @AuthenticationPrincipal user: Any,
        @Parameter(description = "Transaction id")
        @PathVariable transactionId: String
    ): TransactionResponseDto = transactionService.getTransaction(transactionId, user)
}

@Schema(description = "Paginated response of Transactions")
private data class TransactionPageResponseDto(
    val content: List<TransactionResponseDto>,
    val totalPages: Int,
    val totalElements: Long,
    val page: Int,
    val size: Int
)