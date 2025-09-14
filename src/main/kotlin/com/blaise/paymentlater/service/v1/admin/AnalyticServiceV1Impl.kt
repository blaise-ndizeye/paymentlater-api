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

/**
 * Implementation of admin analytics service providing business intelligence and reporting.
 * 
 * This service acts as an orchestrator layer that delegates analytics processing
 * to specialized repository extension methods while providing:
 * - Consistent logging and monitoring
 * - Data transformation (Page to PageResponseDto)
 * - Service-level error handling and validation
 * 
 * The implementation maintains separation of concerns by keeping business logic
 * in repository layers while handling service-level concerns like logging,
 * transformation, and response formatting.
 * 
 * @property adminRepository Repository for admin-specific operations (system health)
 * @property merchantRepository Repository for merchant analytics operations
 * @property transactionRepository Repository for transaction analytics operations
 * @property refundRepository Repository for refund analytics operations
 */
@Service
class AnalyticServiceV1Impl(
    private val adminRepository: AdminRepository,
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository,
    private val refundRepository: RefundRepository
) : AnalyticServiceV1 {

    /**
     * {@inheritDoc}
     * 
     * Delegates to merchant repository for data aggregation and returns
     * comprehensive merchant performance metrics.
     */
    override fun getMerchantsOverview(filter: MerchantOverviewFilterDto): MerchantOverviewResponseDto {
        return merchantRepository.getMerchantsOverview(filter)
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to transaction repository for currency-safe analytics processing,
     * transforms Spring Data Page to API PageResponseDto, and logs operation metrics.
     */
    override fun getTransactionsOverview(
        filter: TransactionOverviewFilterDto
    ): PageResponseDto<TransactionOverviewResponseDto> {
        return transactionRepository.getTransactionOverview(filter)
            .toPageResponseDto()
            .also { log.info { "Found ${it.totalElements} transaction overviews" } }
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to refund repository for approval rate analytics processing,
     * transforms Spring Data Page to API PageResponseDto, and logs operation metrics.
     */
    override fun getRefundsOverview(filter: RefundOverviewFilterDto): PageResponseDto<RefundOverviewResponseDto> {
        return refundRepository.getRefundsOverview(filter)
            .toPageResponseDto()
            .also { log.info { "Found ${it.totalElements} refund overviews" } }
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to admin repository for system health metrics collection
     * within the specified time window and logs health check operation.
     */
    override fun getSystemHealthOverview(windowHours: Long): SystemHealthResponseDto {
        return adminRepository.getSystemHealthOverview(windowHours)
            .also { log.info { "Retrieved system health metrics for $windowHours hour window" } }
    }
}