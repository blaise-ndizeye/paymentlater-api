package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.extension.toAdminResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.RefreshTokenRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.service.v1.admin.AdminServiceV1
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/auth")
class AdminAuthControllerV1(
    private val adminService: AdminServiceV1
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: AdminLoginRequestDto
    ): TokenResponseDto = adminService.login(body)

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: AdminRegisterRequestDto
    ): AdminResponseDto = adminService.register(body)

    @PostMapping("/refresh-token")
    fun refreshToken(@Valid @RequestBody body: RefreshTokenRequestDto): TokenResponseDto =
        adminService.refreshToken(body.refreshToken)

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    fun me(@AuthenticationPrincipal admin: Admin): AdminResponseDto = admin.toAdminResponseDto()

}