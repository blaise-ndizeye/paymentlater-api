package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.Currency
import org.bson.types.ObjectId
import java.time.Instant

data class RefundOverviewFilterDto(
    val start: Instant? = null,
    val end: Instant? = null,
    val merchantId: ObjectId? = null,
    val currencies: List<Currency>? = null,
    val page: Int = 0,
    val size: Int = 20
)
