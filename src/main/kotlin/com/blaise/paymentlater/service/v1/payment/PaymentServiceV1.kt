package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto

interface PaymentServiceV1 {
    fun getPayments(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto>

    fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto
}