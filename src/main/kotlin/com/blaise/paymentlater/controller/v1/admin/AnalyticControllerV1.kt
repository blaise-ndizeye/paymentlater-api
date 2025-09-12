package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto
import com.blaise.paymentlater.service.v1.admin.AnalyticServiceV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.bson.types.ObjectId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Admin Analytics", description = "Admin Analytics")
class AnalyticControllerV1(
    private val analyticService: AnalyticServiceV1
) {

    @GetMapping("/merchants/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get merchants summary",
        description = "Get merchants summary",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Merchants summary",
                content = [Content(schema = Schema(implementation = MerchantOverviewResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun getMerchantsOverview(
        @Parameter(description = "Start date")
        @RequestParam(name = "createdStart", required = false) createdStart: String? = null,

        @Parameter(description = "End date")
        @RequestParam(name = "createdEnd", required = false) createdEnd: String? = null,
    ): MerchantOverviewResponseDto {
        val filter = MerchantOverviewFilterDto(
            createdStart = createdStart?.let { Instant.parse(it) },
            createdEnd = createdEnd?.let { Instant.parse(it) },
        )

        return analyticService.getMerchantsOverview(filter)
    }

    @GetMapping("/transactions/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get transactions overview",
        description = "Get transactions overview",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Transactions overview",
                content = [Content(schema = Schema(implementation = TransactionOverviewPageResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun getTransactionsOverview(
        @Parameter(description = "Start date")
        @RequestParam(required = false) startDate: String?,

        @Parameter(description = "End date")
        @RequestParam(required = false) endDate: String?,

        @Parameter(description = "Merchant id")
        @RequestParam(required = false) merchantId: String?,

        @Parameter(description = "Transaction status")
        @RequestParam(required = false) statuses: List<String>?,

        @Parameter(description = "Transaction currencies")
        @RequestParam(required = false) currencies: List<String>?,

        @Parameter(description = "Page number")
        @RequestParam(required = false, defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageResponseDto<TransactionOverviewResponseDto> {
        val filter = TransactionOverviewFilterDto(
            startDate = startDate?.let { Instant.parse(it) },
            endDate = endDate?.let { Instant.parse(it) },
            merchantId = merchantId?.let { ObjectId(it) },
            statuses = statuses?.map { TransactionStatus.valueOf(it) },
            currencies = currencies?.map { Currency.valueOf(it) },
            page = page,
            size = size
        )

        return analyticService.getTransactionsOverview(filter)
    }
}

@Schema(description = "Paginated response of Transaction Overview")
private data class TransactionOverviewPageResponseDto(
    val content: List<TransactionOverviewResponseDto>,
    val totalPages: Int,
    val totalElements: Long,
    val page: Int,
    val size: Int
)