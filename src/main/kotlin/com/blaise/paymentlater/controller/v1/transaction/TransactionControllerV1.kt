package com.blaise.paymentlater.controller.v1.transaction

import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
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
import org.springframework.web.bind.annotation.*

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
        summary = "Refund a transaction",
        security = [SecurityRequirement(name = "ApiKey")],
        description = "Refund a transaction",
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
}