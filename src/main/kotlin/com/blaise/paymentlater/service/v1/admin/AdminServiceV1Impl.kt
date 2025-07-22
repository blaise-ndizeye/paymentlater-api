package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.extension.toAdminResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.repository.AdminRepository
import com.blaise.paymentlater.security.admin.HashEncoderConfig
import com.blaise.paymentlater.security.admin.JwtConfig
import com.blaise.paymentlater.service.v1.refreshtoken.RefreshTokenService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

private val log = KotlinLogging.logger {}

@Service
class AdminServiceV1Impl(
    private val adminRepository: AdminRepository,
    private val hashEncoderConfig: HashEncoderConfig,
    private val jwtConfig: JwtConfig,
    private val refreshTokenService: RefreshTokenService
) : AdminServiceV1 {

    override fun login(body: AdminLoginRequestDto): TokenResponseDto {
        val admin = findByUsername(body.username)
        if (!hashEncoderConfig.matches(body.password, admin.password)) {
            log.warn { "Invalid login attempt for admin username: ${body.username}" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials")
        }

        val accessToken = jwtConfig.generateAccessToken(admin.username)
        val refreshToken = jwtConfig.generateRefreshToken(admin.username)
        refreshTokenService.saveRefreshToken(admin.id, refreshToken)

        return TokenResponseDto(accessToken, refreshToken).also {
            log.info { "Admin logged in: ${admin.username}" }
        }
    }

    override fun register(body: AdminRegisterRequestDto): AdminResponseDto {
        val adminExists = existsByUsername(body.username)
        if (adminExists)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists")

        return adminRepository.save(
            Admin(
                username = body.username,
                password = hashEncoderConfig.encode(body.password),
            )
        ).toAdminResponseDto().also { admin ->
            log.info { "Admin registered: ${admin.username}" }
        }
    }

    @Transactional
    override fun refreshToken(oldRefreshToken: String): TokenResponseDto {
        val exception = ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh token")
        if (!jwtConfig.validateRefreshToken(oldRefreshToken)) throw exception

        val username = jwtConfig.getUsernameFromToken(oldRefreshToken)
        val user = findByUsername(username)

        refreshTokenService.findByUserIdAndToken(
            user.id,
            hashEncoderConfig.hashLongString(oldRefreshToken)
        ) ?: throw exception
        refreshTokenService.deleteByUserIdAndToken(user.id, oldRefreshToken)

        val newAccessToken = jwtConfig.generateAccessToken(user.username)
        val newRefreshToken = jwtConfig.generateRefreshToken(user.username)
        refreshTokenService.saveRefreshToken(user.id, newRefreshToken)

        return TokenResponseDto(newAccessToken, newRefreshToken)
    }

    override fun findByUsername(username: String): Admin =
        adminRepository.findByUsername(username) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid credentials"
        )

    override fun existsByUsername(username: String): Boolean = adminRepository.existsByUsername(username)

    override fun getAuthenticatedAdmin(): Admin {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Admin)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return principal
    }
}