package com.blaise.paymentlater.controller.v1.refund

import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}