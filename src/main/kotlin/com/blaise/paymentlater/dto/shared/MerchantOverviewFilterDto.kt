package com.blaise.paymentlater.dto.shared

import java.time.Instant

data class MerchantOverviewFilterDto(
    val createdStart: Instant? = null,
    val createdEnd: Instant? = null
)