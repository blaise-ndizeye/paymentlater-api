package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.shared.MerchantOverviewFilter
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.repository.MerchantRepository
import org.springframework.stereotype.Service

@Service
class AnalyticServiceV1Impl(
    private val merchantRepository: MerchantRepository
) : AnalyticServiceV1 {
    override fun getMerchantsOverview(filter: MerchantOverviewFilter): MerchantOverviewResponseDto {
        return merchantRepository.getMerchantsOverview(filter)
    }
}