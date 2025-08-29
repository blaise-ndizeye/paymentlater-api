package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.model.Merchant

data class RegisterMerchantEventDto(
    val merchant: Merchant,
    val apiKey: String
)