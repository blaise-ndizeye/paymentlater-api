package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.config.HashEncoderConfig
import com.blaise.paymentlater.notification.MailService
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.security.merchant.ApiKeyConfig
import com.blaise.paymentlater.util.TestFactory
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class MerchantAuthServiceV1ImplTest {
    private val merchantRepository: MerchantRepository = mockk()
    private val apiKeyConfig: ApiKeyConfig = mockk()
    private val mailService: MailService = mockk(relaxed = true)
    private val hashEncoderConfig: HashEncoderConfig = mockk()
    private lateinit var merchantAuthService: MerchantAuthServiceV1Impl

    @BeforeEach
    fun setup() {
        merchantAuthService = MerchantAuthServiceV1Impl(
            merchantRepository,
            apiKeyConfig,
            mailService,
            hashEncoderConfig
        )
        clearMocks(merchantRepository, apiKeyConfig, mailService)
    }

    @Nested
    @DisplayName("REGISTER")
    inner class Register {

        @Test
        fun `should register a new merchant`() {
            val apiKey = "fake-api-key"
            val apiKeyDigest = "fake-api-key-digest"
            val apiKeyHash = "fake-api-key-hash"
            val registerDto = TestFactory.merchantRegisterRequestDto()
            val registeredMerchant = TestFactory.merchant1()

            every { merchantRepository.existsByEmail(registerDto.email) } returns false
            every { apiKeyConfig.generateApiKey() } returns apiKey
            every { hashEncoderConfig.digest(apiKey) } returns apiKeyDigest
            every { hashEncoderConfig.encode(apiKey) } returns apiKeyHash
            every { merchantRepository.save(any()) } returns registeredMerchant
            every {
                mailService.sendMerchantRegisterApiKeyEmail(registerDto.email, registerDto.name, apiKey)
            } just Runs

            val result = merchantAuthService.register(registerDto)
            val responseDto = TestFactory.merchantResponseDto()

            assertEquals(responseDto.id, result.id)
            assertEquals(responseDto.email, result.email)

            verify(exactly = 1) { merchantRepository.existsByEmail(registerDto.email) }
            verify(exactly = 1) { apiKeyConfig.generateApiKey() }
            verify(exactly = 1) { merchantRepository.save(any()) }
        }

        @Test
        fun `should throw exception if merchant already exists`() {
            val registerDto = TestFactory.merchantRegisterRequestDto()

            every { merchantRepository.existsByEmail(registerDto.email) } returns true

            assertThrows<ResponseStatusException> {
                merchantAuthService.register(registerDto)
            }

            verify(exactly = 1) { merchantRepository.existsByEmail(registerDto.email) }
            verify(exactly = 0) { apiKeyConfig.generateApiKey() }
        }
    }

    @Nested
    @DisplayName("REGENERATE API KEY")
    inner class RegenerateApiKey {

        @Test
        fun `should regenerate API key`() {
            val email = "john@doe"
            val merchant = TestFactory.merchant1()
            val apiKey = "fake-api-key"
            val apiKeyDigest = "fake-api-key-digest"
            val apiKeyHash = "fake-api-key-hash"

            every { merchantRepository.findByEmail(email) } returns merchant
            every { apiKeyConfig.generateApiKey() } returns apiKey
            every { hashEncoderConfig.digest(apiKey) } returns apiKeyDigest
            every { hashEncoderConfig.encode(apiKey) } returns apiKeyHash
            every { merchantRepository.save(any()) } returns merchant
            every {
                mailService.sendRegenerateApiKeyEmail(email, merchant.name, apiKey)
            } just Runs

            val result = merchantAuthService.regenerateApiKey(email)

            assertEquals(HttpStatus.OK, result.statusCode)
            verify(exactly = 1) { merchantRepository.findByEmail(email) }
            verify(exactly = 1) { apiKeyConfig.generateApiKey() }
            verify(exactly = 1) { merchantRepository.save(any()) }
        }

        @Test
        fun `should throw exception if merchant not found`() {
            val email = "john@doe"

            every { merchantRepository.findByEmail(email) } returns null

            assertThrows<ResponseStatusException> {
                merchantAuthService.regenerateApiKey(email)
            }

            verify(exactly = 1) { merchantRepository.findByEmail(email) }
            verify(exactly = 0) { apiKeyConfig.generateApiKey() }
        }
    }

    @Nested
    @DisplayName("SET WEBHOOK")
    inner class SetWebhook {

        @Test
        fun `should set webhook`() {
            val webhookUrl = "https://example.com/webhook"
            val merchant = TestFactory.merchant1()
            val merchantAuthServiceSpy = spyk(merchantAuthService)

            every { merchantAuthServiceSpy.getAuthenticatedMerchant() } returns merchant
            every { merchantRepository.save(any()) } returns merchant

            val result = merchantAuthServiceSpy.setWebhook(webhookUrl)

            assertEquals(HttpStatus.OK, result.statusCode)
            verify(exactly = 1) { merchantRepository.save(any()) }
        }

        @Test
        fun `should throw exception if merchant not found`() {
            val webhookUrl = "https://example.com/webhook"

            val merchantAuthServiceSpy = spyk(merchantAuthService)
            every {
                merchantAuthServiceSpy.getAuthenticatedMerchant()
            } throws ResponseStatusException(HttpStatus.UNAUTHORIZED)

            assertThrows<ResponseStatusException> {
                merchantAuthServiceSpy.setWebhook(webhookUrl)
            }

            verify(exactly = 1) { merchantAuthServiceSpy.getAuthenticatedMerchant() }
            verify(exactly = 0) { merchantRepository.save(any()) }
        }
    }
}