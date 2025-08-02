package com.blaise.paymentlater.domain.model.sub

data class PaymentMetadata(
    val referenceId: String,
    val userId: String?,
    val phone: String?,
    val email: String?,
    val description: String?
)