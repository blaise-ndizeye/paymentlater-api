package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto

interface AdminServiceV1 {
    fun login(body: AdminLoginRequestDto): TokenResponseDto

    fun register(body: AdminRegisterRequestDto): AdminResponseDto

    fun getAuthenticatedAdmin(): Admin

    fun refreshToken(oldRefreshToken: String): TokenResponseDto

    fun findByUsername(username: String): Admin

    fun existsByUsername(username: String): Boolean
}