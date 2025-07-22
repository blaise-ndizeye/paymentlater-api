package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantResponseDto

interface MerchantServiceV1 {
    fun register(body: MerchantRegisterRequestDto): MerchantResponseDto

    fun findByEmail(email: String): Merchant

    fun findByApiKey(apiKey: String): Merchant?

    fun existsByEmail(email: String): Boolean

    fun existsByApiKey(apiKey: String): Boolean

    fun getAuthenticatedMerchant(): Merchant
}