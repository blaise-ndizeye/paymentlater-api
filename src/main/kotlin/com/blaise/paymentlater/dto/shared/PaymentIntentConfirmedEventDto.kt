package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Transaction

data class PaymentIntentConfirmedEventDto(
    val merchant: Merchant,
    val paymentIntent: PaymentIntent,
    val transaction: Transaction
)