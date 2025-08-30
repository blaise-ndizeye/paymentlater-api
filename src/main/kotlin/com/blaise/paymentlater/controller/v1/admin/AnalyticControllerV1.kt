package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilter
import com.blaise.paymentlater.service.v1.admin.AnalyticServiceV1
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
        val filter = MerchantOverviewFilter(
            createdStart = createdStart?.let { Instant.parse(it) },
            createdEnd = createdEnd?.let { Instant.parse(it) },
        )

        return analyticService.getMerchantsOverview(filter)
    }
}