package com.blaise.paymentlater.service

import com.blaise.paymentlater.domain.extension.toMerchantResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantResponseDto
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.security.merchant.ApiKeyConfig
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@Service
class MerchantService(
    private val merchantRepository: MerchantRepository,
    private val apiKeyConfig: ApiKeyConfig
) {

    private fun generateUniqueApiKey(): String {
        var key: String
        do {
            key = apiKeyConfig.generateApiKey()
        } while (existsByApiKey(key))
        return key
    }

    fun register(body: MerchantRegisterRequestDto): MerchantResponseDto {
        val merchantExists = existsByEmail(body.email)
        if (merchantExists)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists")

        val newMerchant = Merchant(
            name = body.name,
            email = body.email,
            apiKey = generateUniqueApiKey()
        )
        return merchantRepository.save(newMerchant).toMerchantResponseDto().also {
            logger.info { "Merchant registered: ${it.id}" }
        }
    }

    fun findByEmail(email: String): Merchant = merchantRepository.findByEmail(email)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Merchant not found")

    fun findByApiKey(apiKey: String): Merchant? = merchantRepository.findByApiKey(apiKey)

    fun existsByEmail(email: String): Boolean = merchantRepository.existsByEmail(email)

    fun existsByApiKey(apiKey: String): Boolean = merchantRepository.existsByApiKey(apiKey)

    fun getAuthenticatedMerchant(): Merchant {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is Merchant)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return principal
    }
}