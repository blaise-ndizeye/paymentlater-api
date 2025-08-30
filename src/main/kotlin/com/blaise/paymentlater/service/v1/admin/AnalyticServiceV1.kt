package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.shared.MerchantOverviewFilter
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto

interface AnalyticServiceV1 {
    fun getMerchantsOverview(filter: MerchantOverviewFilter): MerchantOverviewResponseDto
}