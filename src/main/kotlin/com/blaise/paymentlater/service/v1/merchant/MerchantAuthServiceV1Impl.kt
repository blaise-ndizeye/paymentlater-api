package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.domain.extension.toMerchantResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantResponseDto
import com.blaise.paymentlater.notification.MailService
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.security.merchant.ApiKeyConfig
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

private val log = KotlinLogging.logger {}

@Service
class MerchantAuthServiceV1Impl(
    private val merchantRepository: MerchantRepository,
    private val apiKeyConfig: ApiKeyConfig,
    private val mailService: MailService
) : MerchantAuthServiceV1 {

    private fun generateUniqueApiKey(): String {
        var key: String
        do {
            key = apiKeyConfig.generateApiKey()
        } while (existsByApiKey(key))
        return key
    }

    override fun register(body: MerchantRegisterRequestDto): MerchantResponseDto {
        val merchantExists = existsByEmail(body.email)
        if (merchantExists)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists")

        val newMerchant = Merchant(
            name = body.name,
            email = body.email,
            apiKey = generateUniqueApiKey()
        )
        return merchantRepository.save(newMerchant).toMerchantResponseDto().also {
            log.info { "Merchant registered: ${it.id}" }
        }
    }

    override fun findByEmail(email: String): Merchant = merchantRepository.findByEmail(email)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Merchant not found")

    override fun findByApiKey(apiKey: String): Merchant? = merchantRepository.findByApiKey(apiKey)

    override fun existsByEmail(email: String): Boolean = merchantRepository.existsByEmail(email)

    override fun existsByApiKey(apiKey: String): Boolean = merchantRepository.existsByApiKey(apiKey)

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

        val newApiKey = generateUniqueApiKey()
        val updatedMerchant = merchant.copy(apiKey = newApiKey)
        merchantRepository.save(updatedMerchant)
        mailService.sendApiKeyEmail(merchant.email, merchant.name, newApiKey)
        log.info { "Merchant ${merchant.id} regenerated API key!" }
        return ResponseEntity.ok().build()
    }
}