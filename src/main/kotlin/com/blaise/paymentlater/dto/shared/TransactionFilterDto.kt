package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.TransactionStatus
import org.bson.types.ObjectId
import java.time.Instant

data class TransactionFilterDto(
    val currencies: List<Currency>? = null,

    val statuses: List<TransactionStatus>? = null,

    val start: Instant? = null,

    val end: Instant? = null,

    val merchantId: ObjectId? = null
)
