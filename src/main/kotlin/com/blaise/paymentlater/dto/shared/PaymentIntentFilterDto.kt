package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.PaymentStatus
import org.bson.types.ObjectId
import java.time.Instant

data class PaymentIntentFilterDto(
    val merchantId: ObjectId? = null,
    val start: Instant? = null,
    val end: Instant? = null,
    val statuses: List<PaymentStatus>? = null,
    val currencies: List<Currency>? = null,
)
