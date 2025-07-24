package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.model.RefreshToken
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto
import com.blaise.paymentlater.repository.AdminRepository
import com.blaise.paymentlater.security.admin.HashEncoderConfig
import com.blaise.paymentlater.security.admin.JwtConfig
import com.blaise.paymentlater.service.v1.refreshtoken.RefreshTokenService
import com.blaise.paymentlater.util.TestFactory
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AdminAuthServiceV1ImplTest {
    private val adminRepository: AdminRepository = mockk()
    private val hashEncoderConfig: HashEncoderConfig = mockk()
    private val jwtConfig: JwtConfig = mockk()
    private val refreshTokenService: RefreshTokenService = mockk()
    private lateinit var adminAuthService: AdminAuthServiceV1Impl
    private val adminData = TestFactory.admin()

    @BeforeEach
    fun setUp() {
        adminAuthService = AdminAuthServiceV1Impl(
            adminRepository,
            hashEncoderConfig,
            jwtConfig,
            refreshTokenService
        )
    }

    @BeforeEach
    fun resetMocks() {
        clearMocks(adminRepository, hashEncoderConfig, jwtConfig, refreshTokenService)
    }

    @Nested
    @DisplayName("LOGIN")
    inner class Login {

        @Test
        fun `login should return tokens if credentials are valid`() {
            val loginDto = TestFactory.adminLoginRequestDto()

            every { adminRepository.findByUsername(loginDto.username) } returns adminData
            every { hashEncoderConfig.matches(loginDto.password, adminData.password) } returns true
            every { jwtConfig.generateAccessToken("admin1") } returns "accessToken"
            every { jwtConfig.generateRefreshToken("admin1") } returns "refreshToken"
            every { refreshTokenService.saveRefreshToken(adminData.id, "refreshToken") } just Runs

            val result = adminAuthService.login(loginDto)

            assertEquals("accessToken", result.accessToken)
            assertEquals("refreshToken", result.refreshToken)

            verify(exactly = 1) { adminRepository.findByUsername(loginDto.username) }
            verify(exactly = 1) { hashEncoderConfig.matches(loginDto.password, adminData.password) }
            verify(exactly = 1) { jwtConfig.generateAccessToken("admin1") }
            verify(exactly = 1) { jwtConfig.generateRefreshToken("admin1") }
            verify(exactly = 1) { refreshTokenService.saveRefreshToken(adminData.id, "refreshToken") }
        }

        @Test
        fun `should throw exception if credentials are invalid`() {
            val loginDto = TestFactory.adminLoginRequestDto()
            val admin = TestFactory.admin()

            every { adminRepository.findByUsername(loginDto.username) } returns admin
            every { hashEncoderConfig.matches(loginDto.password, any()) } returns false

            assertThrows<ResponseStatusException> {
                adminAuthService.login(loginDto)
            }

            verify(exactly = 1) { adminRepository.findByUsername(loginDto.username) }
            verify(exactly = 1) { hashEncoderConfig.matches(loginDto.password, any()) }
        }

        @Test
        fun `should throw exception if admin does not exist`() {
            val loginDto = TestFactory.adminLoginRequestDto()

            every { adminRepository.findByUsername(loginDto.username) } returns null

            assertThrows<ResponseStatusException> {
                adminAuthService.login(loginDto)
            }

            verify(exactly = 0) { hashEncoderConfig.matches(loginDto.password, any()) }
            verify(exactly = 1) { adminRepository.findByUsername(loginDto.username) }
        }
    }

    @Nested
    @DisplayName("REGISTER")
    inner class Register {

        @Test
        fun `should register a new admin`() {
            val registerDto = TestFactory.adminRegisterRequestDto()

            every { adminRepository.existsByUsername(registerDto.username) } returns false
            every { hashEncoderConfig.encode(registerDto.password) } returns "encode123"
            every { adminRepository.save(any()) } returns adminData

            val result = adminAuthService.register(registerDto)
            val responseDto = AdminResponseDto(
                adminData.id.toString(),
                "admin1",
                result.createdAt
            )

            assertEquals(responseDto, result)
            assertTrue(
                Instant.now().minusSeconds(5)
                    .isBefore(
                        Instant.parse(result.createdAt)
                    )
            )

            verify(exactly = 1) { adminRepository.existsByUsername(registerDto.username) }
            verify(exactly = 1) { hashEncoderConfig.encode(registerDto.password) }
            verify(exactly = 1) { adminRepository.save(any()) }
        }

        @Test
        fun `should throw exception if admin already exists`() {
            val registerDto = TestFactory.adminRegisterRequestDto()

            every { adminRepository.existsByUsername(registerDto.username) } returns true

            assertThrows<ResponseStatusException> {
                adminAuthService.register(registerDto)
            }

            verify(exactly = 1) { adminRepository.existsByUsername(registerDto.username) }
            verify(exactly = 0) { hashEncoderConfig.encode(any()) }
        }
    }

    @Nested
    @DisplayName("TOKEN")
    inner class TOKEN {

        @Test
        fun `should refresh an old admin token`() {
            val oldRefreshToken = "oldRefreshToken"
            val newAccessToken = "newAccessToken"
            val newRefreshToken = "newRefreshToken"
            val admin = TestFactory.admin()

            every { jwtConfig.refreshTokenValidityMillis } returns 1000
            val refreshTokenData = RefreshToken(
                ObjectId(),
                admin.id,
                token = oldRefreshToken,
                expiresAt = Instant.now().plusMillis(jwtConfig.refreshTokenValidityMillis),
            )

            every { jwtConfig.validateRefreshToken(oldRefreshToken) } returns true
            every { jwtConfig.getUsernameFromToken(oldRefreshToken) } returns "admin1"
            every { adminRepository.findByUsername("admin1") } returns admin
            every { hashEncoderConfig.hashLongString(oldRefreshToken) } returns newRefreshToken
            every { refreshTokenService.findByUserIdAndToken(any(), any()) } returns refreshTokenData
            every { refreshTokenService.deleteByUserIdAndToken(any(), any()) } just Runs
            every { jwtConfig.generateAccessToken("admin1") } returns newAccessToken
            every { jwtConfig.generateRefreshToken("admin1") } returns newRefreshToken
            every { refreshTokenService.saveRefreshToken(any(), any()) } just Runs

            val result = adminAuthService.refreshToken(oldRefreshToken)

            assertEquals(TokenResponseDto(newAccessToken, newRefreshToken), result)
            verify(exactly = 1) { jwtConfig.validateRefreshToken(oldRefreshToken) }
            verify(exactly = 1) { jwtConfig.getUsernameFromToken(oldRefreshToken) }
            verify(exactly = 1) { adminRepository.findByUsername("admin1") }
            verify(exactly = 1) { refreshTokenService.findByUserIdAndToken(any(), any()) }
            verify(exactly = 1) { refreshTokenService.deleteByUserIdAndToken(any(), any()) }
            verify(exactly = 1) { jwtConfig.generateAccessToken("admin1") }
            verify(exactly = 1) { jwtConfig.generateRefreshToken("admin1") }
            verify(exactly = 1) { refreshTokenService.saveRefreshToken(any(), any()) }
        }

        @Test
        fun `should throw exception if refresh token is invalid`() {
            val oldRefreshToken = "oldRefreshToken"

            every { jwtConfig.validateRefreshToken(oldRefreshToken) } returns false

            assertThrows<ResponseStatusException> {
                adminAuthService.refreshToken(oldRefreshToken)
            }

            verify(exactly = 1) { jwtConfig.validateRefreshToken(oldRefreshToken) }
            verify(exactly = 0) { jwtConfig.getUsernameFromToken(oldRefreshToken) }
        }

        @Test
        fun `should throw exception if refresh token data is not found in database`() {
            val oldRefreshToken = "oldRefreshToken"
            val hashedOldRefreshToken = "hashedOldRefreshToken"
            val username = "admin1"

            every { jwtConfig.validateRefreshToken(oldRefreshToken) } returns true
            every { jwtConfig.getUsernameFromToken(oldRefreshToken) } returns username
            every { adminAuthService.findByUsername(username) } returns adminData
            every { refreshTokenService.findByUserIdAndToken(any(), any()) } returns null
            every { hashEncoderConfig.hashLongString(oldRefreshToken) } returns hashedOldRefreshToken

            assertThrows<ResponseStatusException> {
                adminAuthService.refreshToken(oldRefreshToken)
            }

            verify(exactly = 1) { jwtConfig.validateRefreshToken(oldRefreshToken) }
            verify(exactly = 1) { jwtConfig.getUsernameFromToken(oldRefreshToken) }
            verify(exactly = 1) { adminAuthService.findByUsername(username) }
            verify(exactly = 1) { refreshTokenService.findByUserIdAndToken(any(), any()) }
            verify(exactly = 0) { refreshTokenService.deleteByUserIdAndToken(any(), any()) }
        }
    }
}