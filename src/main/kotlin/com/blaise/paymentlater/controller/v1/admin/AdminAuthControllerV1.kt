package com.blaise.paymentlater.controller.v1.admin

import com.blaise.paymentlater.domain.extension.toAdminResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.service.v1.admin.AdminServiceV1
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/auth")
class AdminAuthControllerV1(
    private val adminService: AdminServiceV1
) {

    @PostMapping("/login")
    fun login(body: AdminLoginRequestDto): TokenResponseDto = adminService.login(body)

    @PostMapping("/register")
    fun register(body: AdminRegisterRequestDto): AdminResponseDto = adminService.register(body)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal admin: Admin): AdminResponseDto = admin.toAdminResponseDto()

}