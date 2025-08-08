package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import java.time.Instant

interface PaymentServiceV1 {
    fun getPayments(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto>

    fun getPayment(id: String, user: Any): PaymentIntentResponseDto

    fun findById(id: String): PaymentIntent

    fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto

    fun expireOldPaymentIntents(now: Instant)
}