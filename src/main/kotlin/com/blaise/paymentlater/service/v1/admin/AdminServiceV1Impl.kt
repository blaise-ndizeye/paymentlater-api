package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.repository.AdminRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AdminServiceV1Impl(
    private val adminRepository: AdminRepository
) : AdminServiceV1 {
    override fun login(body: AdminLoginRequestDto): TokenResponseDto {
        TODO("Not yet implemented")
    }

    override fun register(body: AdminRegisterRequestDto): AdminResponseDto {
        TODO()
    }

    override fun getAuthenticatedAdmin(): Admin {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Admin)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return principal
    }

    override fun findByUsername(username: String): Admin =
        adminRepository.findByUsername(username) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Invalid username"
        )

    override fun existsByUsername(username: String): Boolean = adminRepository.existsByUsername(username)
}