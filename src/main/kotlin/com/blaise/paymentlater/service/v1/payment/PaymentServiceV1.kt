package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import java.time.Instant

interface PaymentServiceV1 {
    fun getPayments(
        statuses: List<PaymentStatus>?,
        currencies: List<Currency>?,
        start: Instant?,
        end: Instant?,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto>

    fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto
}