package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.model.PaymentIntent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant

interface PaymentIntentExtensionRepository {
    fun findByAdminFilters(
        statuses: List<PaymentStatus>?,
        currencies: List<String>?,
        start: Instant?,
        end: Instant?,
        pageable: Pageable
    ): Page<PaymentIntent>
}