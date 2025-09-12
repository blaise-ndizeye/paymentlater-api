package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.TransactionStatus
import org.bson.types.ObjectId
import java.time.Instant

data class TransactionOverviewFilterDto(
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val merchantId: ObjectId? = null,
    val statuses: List<TransactionStatus>? = null,
    val currencies: List<Currency>? = null,
    val page: Int = 0,
    val size: Int = 20
)