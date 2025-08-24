package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.RefundStatus
import org.bson.types.ObjectId
import java.time.Instant

data class RefundFilterDto(
    val merchantId: ObjectId? = null,

    val approvedDateStart: Instant? = null,

    val approvedDateEnd: Instant? = null,

    val rejectedDateStart: Instant? = null,

    val rejectedDateEnd: Instant? = null,

    val statuses: List<RefundStatus>? = null,

    val currencies: List<Currency>? = null,
)
