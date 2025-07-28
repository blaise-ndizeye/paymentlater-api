package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.extension.toAdminResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.RefreshTokenRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.service.v1.admin.AdminAuthServiceV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "Admin Auth", description = "Admin authentication endpoints")
class AdminAuthControllerV1(
    private val adminAuthService: AdminAuthServiceV1
) {

    @PostMapping("/login")
    @Operation(
        summary = "Get login credentials for admin",
        description = "Get login credentials for admin",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AdminLoginRequestDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid credentials", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun login(
        @Valid @RequestBody body: AdminLoginRequestDto
    ): TokenResponseDto = adminAuthService.login(body)

    @PostMapping("/register")
    @Operation(
        summary = "Register a new admin user",
        description = "Register a new admin user",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AdminRegisterRequestDto::class)
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
        @Valid @RequestBody body: AdminRegisterRequestDto
    ): AdminResponseDto = adminAuthService.register(body)

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh token for admin",
        description = "Refresh token for admin",
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponseDto::class)
                    ),
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid refresh token", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ApiErrorResponseDto::class)
                    ),
                ]
            ),
        ]
    )
    fun refreshToken(@Valid @RequestBody body: RefreshTokenRequestDto): TokenResponseDto =
        adminAuthService.refreshToken(body.refreshToken)

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerToken")
    @Operation(
        summary = "Get admin profile",
        description = "Get admin profile",
        security = [SecurityRequirement(name = "ApiKey")],
        responses = [
            ApiResponse(
                responseCode = "200", content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AdminResponseDto::class)
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
    fun me(@AuthenticationPrincipal admin: Admin): AdminResponseDto = admin.toAdminResponseDto()

}