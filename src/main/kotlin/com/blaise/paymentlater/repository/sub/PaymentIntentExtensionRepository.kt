package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import org.springframework.data.domain.Page
import java.time.Instant

interface PaymentIntentExtensionRepository {
    fun search(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): Page<PaymentIntent>

    fun findPendingWithExpiredAtBefore(now: Instant): List<PaymentIntent>
}