package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundOverviewResponseDto
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilterDto
import com.blaise.paymentlater.dto.shared.RefundOverviewFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto

interface AnalyticServiceV1 {
    fun getMerchantsOverview(filter: MerchantOverviewFilterDto): MerchantOverviewResponseDto

    fun getTransactionsOverview(filter: TransactionOverviewFilterDto): PageResponseDto<TransactionOverviewResponseDto>

    fun getRefundsOverview(filter: RefundOverviewFilterDto): PageResponseDto<RefundOverviewResponseDto>
}