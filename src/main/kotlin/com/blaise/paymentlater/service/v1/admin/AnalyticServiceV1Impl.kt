package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundOverviewResponseDto
import com.blaise.paymentlater.dto.response.SystemHealthResponseDto
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilterDto
import com.blaise.paymentlater.dto.shared.RefundOverviewFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto
import com.blaise.paymentlater.repository.AdminRepository
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.repository.RefundRepository
import com.blaise.paymentlater.repository.TransactionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class AnalyticServiceV1Impl(
    private val adminRepository: AdminRepository,
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository,
    private val refundRepository: RefundRepository
) : AnalyticServiceV1 {
    override fun getMerchantsOverview(filter: MerchantOverviewFilterDto): MerchantOverviewResponseDto {
        return merchantRepository.getMerchantsOverview(filter)
    }

    override fun getTransactionsOverview(
        filter: TransactionOverviewFilterDto
    ): PageResponseDto<TransactionOverviewResponseDto> {
        return transactionRepository.getTransactionOverview(filter)
            .toPageResponseDto()
            .also { log.info { "Found ${it.totalElements} transaction overviews" } }
    }

    override fun getRefundsOverview(filter: RefundOverviewFilterDto): PageResponseDto<RefundOverviewResponseDto> {
        return refundRepository.getRefundsOverview(filter)
            .toPageResponseDto()
            .also { log.info { "Found ${it.totalElements} refund overviews" } }
    }

    override fun getSystemHealthOverview(windowHours: Long): SystemHealthResponseDto {
        return adminRepository.getSystemHealthOverview(windowHours)
            .also { log.info { "Review system's health of $windowHours window-hours" } }
    }
}