package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import java.time.Instant

interface AdminPaymentServiceV1 {
    fun search(
        statuses: List<PaymentStatus>?,
        currencies: List<Currency>?,
        start: Instant?,
        end: Instant?,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto>
}