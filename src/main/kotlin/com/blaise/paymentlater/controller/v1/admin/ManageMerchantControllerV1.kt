package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.enum.UserRole
import com.blaise.paymentlater.dto.request.UpdateMerchantRequestDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import com.blaise.paymentlater.service.v1.admin.ManageMerchantServiceV1
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/admin/merchants")
@Tag(name = "Admin Merchant Management", description = "Merchant management endpoints")
class ManageMerchantControllerV1(
    private val manageMerchantService: ManageMerchantServiceV1
) {

    @PatchMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Update a merchant",
        description = "Update a merchant",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Merchant updated",
                content = [Content(schema = Schema(implementation = MerchantProfileResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Merchant not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun updateMerchant(
        @Parameter(description = "Merchant id")
        @PathVariable merchantId: String,

        @Valid @RequestBody body: UpdateMerchantRequestDto
    ): MerchantProfileResponseDto = manageMerchantService.updateMerchant(merchantId, body)

    @PatchMapping("/{merchantId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Deactivate a merchant",
        description = "Deactivate a merchant",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(responseCode = "200", description = "Merchant deactivated"),
            ApiResponse(
                responseCode = "400",
                description = "Merchant is already inactive",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun deactivateMerchant(
        @Parameter(description = "Merchant id")
        @PathVariable merchantId: String
    ): ResponseEntity<Unit> = manageMerchantService.deactivateMerchant(merchantId)


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get all merchants",
        description = "Get all merchants",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Merchants found",
                content = [Content(schema = Schema(implementation = PageResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun getAllMerchants(
        @Parameter(description = "Name of the merchant")
        @RequestParam(name = "name", required = false) name: String?,

        @Parameter(description = "Email of the merchant")
        @RequestParam(name = "email", required = false) email: String?,

        @Parameter(description = "Is active")
        @RequestParam(name = "isActive", required = false) isActive: Boolean?,

        @Parameter(description = "Roles of the merchant", hidden = true)
        @RequestParam(name = "roles") roles: List<String>?,

        @Parameter(description = "Created start date", example = "2025-01-01T00:00:00Z")
        @RequestParam(name = "createdStartDate", required = false) createdStartDate: String?,

        @Parameter(description = "Created end date", example = "2025-01-01T00:00:00Z")
        @RequestParam(name = "createdEndDate", required = false) createdEndDate: String?,

        @Parameter(description = "Updated start date", example = "2025-01-01T00:00:00Z")
        @RequestParam(name = "updatedStartDate", required = false) updatedStartDate: String?,

        @Parameter(description = "Updated end date", example = "2025-01-01T00:00:00Z")
        @RequestParam(name = "updatedEndDate", required = false) updatedEndDate: String?,

        @Parameter(description = "Page number")
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(name = "size", required = false, defaultValue = "20") size: Int
    ): PageResponseDto<MerchantProfileResponseDto> {
        val userRoles = mutableListOf<UserRole>()
            .apply {
                add(UserRole.MERCHANT)
                addAll(
                    if (roles?.isNotEmpty() == true) roles.map { UserRole.valueOf(it) } else emptyList()
                )
                removeAll(listOf(UserRole.ADMIN))
            }

        val filter = MerchantFilterDto(
            name = name,
            email = email,
            isActive = isActive,
            roles = userRoles.toSet().toList(),
            createdStartDate = createdStartDate?.let { Instant.parse(it) },
            createdEndDate = createdEndDate?.let { Instant.parse(it) },
            updatedStartDate = updatedStartDate?.let { Instant.parse(it) },
            updatedEndDate = updatedEndDate?.let { Instant.parse(it) },
        )

        return manageMerchantService.getAllMerchants(filter, page, size)
    }

    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get merchant by id",
        description = "Get merchant by id",
        security = [SecurityRequirement(name = "BearerToken")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Merchant found",
                content = [Content(schema = Schema(implementation = MerchantProfileResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Merchant not found",
                content = [Content(schema = Schema(implementation = ApiErrorResponseDto::class))]
            )
        ]
    )
    fun getMerchantById(
        @Parameter(description = "Merchant id")
        @PathVariable("merchantId") merchantId: String
    ): MerchantProfileResponseDto = manageMerchantService.getMerchantById(merchantId)
}