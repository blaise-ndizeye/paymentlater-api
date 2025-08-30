package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.dto.shared.BucketCount

data class MerchantOverviewResponseDto(
    val total: Long,
    val active: Long,
    val inactive: Long,
    val activeRatio: Double,
    val createdTrend: List<BucketCount>
)
