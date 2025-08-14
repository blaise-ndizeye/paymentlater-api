package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.domain.model.Transaction

data class RefundUpdateEventDto(
    val refund: Refund,
    val paymentIntent: PaymentIntent,
    val transaction: Transaction,
    val merchant: Merchant
)
