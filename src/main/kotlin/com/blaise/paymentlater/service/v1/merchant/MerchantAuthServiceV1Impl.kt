package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.config.HashEncoderConfig
import com.blaise.paymentlater.domain.extension.toMerchantRegisterResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantRegisterResponseDto
import com.blaise.paymentlater.dto.shared.RegisterMerchantEventDto
import com.blaise.paymentlater.notification.MailService
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.security.merchant.ApiKeyConfig
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

private val log = KotlinLogging.logger {}

/**
 * Implementation of merchant authentication with secure API key management.
 * 
 * Provides comprehensive merchant management with focus on security:
 * - Cryptographically secure API key generation
 * - Double-hashing security: digest for lookups + bcrypt for verification
 * - Event-driven registration process with email notifications
 * - Transactional operations for data consistency
 * - Role-based authorization integration
 * 
 * **Security Architecture**:
 * - API keys are generated using secure random algorithms
 * - Keys are both digested (for fast lookups) and bcrypt hashed (for security)
 * - Email notifications are sent asynchronously via event publishing
 * - Webhook URLs are validated and stored for payment notifications
 */
@Service
class MerchantAuthServiceV1Impl(
    private val merchantRepository: MerchantRepository,
    private val apiKeyConfig: ApiKeyConfig,
    private val mailService: MailService,
    private val hashEncoderConfig: HashEncoderConfig,
    private val eventPublisher: ApplicationEventPublisher
) : MerchantAuthServiceV1 {

    override fun save(merchant: Merchant): Merchant = merchantRepository.save(merchant)

    @Transactional
    override fun register(body: MerchantRegisterRequestDto): MerchantRegisterResponseDto {
        val merchantExists = existsByEmail(body.email)
        if (merchantExists)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists")

        val (rawApiKey, apiKeyDigest, hashedApiKey) = generateKeys()
        val newMerchant = save(
            Merchant(
                name = body.name,
                email = body.email,
                apiKey = hashedApiKey,
                apiKeyDigest = apiKeyDigest,
                webhookUrl = body.webhookUrl
            )
        )

        eventPublisher.publishEvent(
            RegisterMerchantEventDto(
                merchant = newMerchant,
                apiKey = rawApiKey
            )
        )

        return newMerchant.toMerchantRegisterResponseDto()
            .copy(apiKey = rawApiKey)
            .also {
                log.info { "Merchant registered: ${newMerchant.id}" }
            }
    }

    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    override fun findById(id: ObjectId): Merchant = merchantRepository.findById(id)
        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    override fun findByEmail(email: String): Merchant = merchantRepository.findByEmail(email)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Merchant not found")

    override fun findByApiKeyDigest(apiKey: String): Merchant? {
        val digest = hashEncoderConfig.digest(apiKey)
        val merchant = merchantRepository.findByApiKeyDigest(digest)
        return if (merchant != null && hashEncoderConfig.matches(apiKey, merchant.apiKey)) {
            merchant
        } else null
    }

    override fun existsByEmail(email: String): Boolean = merchantRepository.existsByEmail(email)

    override fun getAuthenticatedMerchant(): Merchant {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Merchant)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return principal
    }

    @Transactional
    override fun regenerateApiKey(email: String): ResponseEntity<Unit> {
        val merchant = try {
            findByEmail(email)
        } catch (_: ResponseStatusException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid credentials")
        }

        val (rawApiKey, apiKeyDigest, hashedApiKey) = generateKeys()
        val updatedMerchant = merchant.copy(
            apiKeyDigest = apiKeyDigest,
            apiKey = hashedApiKey
        )

        merchantRepository.save(updatedMerchant)
        mailService.sendRegenerateApiKeyEmail(merchant.email, merchant.name, rawApiKey)
        log.info { "Merchant ${merchant.id} regenerated API key!" }
        return ResponseEntity.ok().build()
    }

    override fun setWebhook(webhookUrl: String): ResponseEntity<Unit> {
        val merchant = getAuthenticatedMerchant()
        val updatedMerchant = merchant.copy(webhookUrl = webhookUrl)
        save(updatedMerchant)
        return ResponseEntity.ok().build()
    }

    private fun generateKeys(): Triple<String, String, String> {
        val rawApiKey = apiKeyConfig.generateApiKey()
        val apiKeyDigest = hashEncoderConfig.digest(rawApiKey)
        val hashedApiKey = hashEncoderConfig.encode(rawApiKey)
        return Triple(rawApiKey, apiKeyDigest, hashedApiKey)
    }
}