package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantRegisterResponseDto
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity

interface MerchantAuthServiceV1 {
    fun register(body: MerchantRegisterRequestDto): MerchantRegisterResponseDto

    fun findById(id: ObjectId): Merchant

    fun findByEmail(email: String): Merchant

    fun findByApiKeyDigest(apiKey: String): Merchant?

    fun existsByEmail(email: String): Boolean

    fun getAuthenticatedMerchant(): Merchant

    fun regenerateApiKey(email: String): ResponseEntity<Unit>

    fun setWebhook(webhookUrl: String): ResponseEntity<Unit>
}