package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundOverviewResponseDto
import com.blaise.paymentlater.dto.response.SystemHealthResponseDto
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilterDto
import com.blaise.paymentlater.dto.shared.RefundOverviewFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto

/**
 * Admin analytics service providing comprehensive business intelligence and reporting.
 * 
 * This service offers various analytics endpoints for administrators to monitor
 * and analyze system performance, merchant activity, and financial metrics:
 * 
 * - **Merchant Analytics**: Comprehensive merchant performance and activity metrics
 * - **Transaction Analytics**: Transaction volume, success rates, and currency-specific insights
 * - **Refund Analytics**: Refund processing efficiency and approval rate analysis
 * - **System Health**: Real-time system health monitoring with configurable time windows
 * 
 * All analytics maintain financial integrity by properly handling multi-currency scenarios
 * without mixing different currency amounts in calculations.
 * 
 * **Security**: This service is intended for admin-level access only and should be
 * properly secured with appropriate authorization checks.
 */
interface AnalyticServiceV1 {

    /**
     * Get comprehensive merchant performance overview.
     * 
     * Provides aggregated merchant statistics and their metrics based on
     * filtering criteria
     * 
     * @param filter Merchant overview filtering criteria (date range)
     * @return Aggregated merchant activity metrics
     */
    fun getMerchantsOverview(filter: MerchantOverviewFilterDto): MerchantOverviewResponseDto

    /**
     * Get transaction analytics with currency-specific breakdowns.
     * 
     * Provides detailed transaction performance metrics grouped by merchant and currency
     * to maintain financial integrity. Includes transaction counts, success rates,
     * total amounts, and average order values.
     * 
     * **Financial Integrity**: Transactions are always grouped by currency to prevent
     * mixing different currency amounts (e.g., USD + EUR) in calculations.
     * 
     * @param filter Transaction filtering criteria (date range, merchant, currencies, etc.)
     * @return Paginated transaction overview with currency-specific metrics
     */
    fun getTransactionsOverview(filter: TransactionOverviewFilterDto): PageResponseDto<TransactionOverviewResponseDto>

    /**
     * Get refund analytics with approval rate insights.
     * 
     * Provides detailed refund processing metrics grouped by merchant and currency.
     * Includes refund counts, approval rates, rejection rates, and total refunded amounts.
     * 
     * **Financial Integrity**: Refunds are always grouped by currency to ensure accurate
     * financial calculations and prevent currency mixing.
     * 
     * @param filter Refund filtering criteria (date range, merchant, currencies, etc.)
     * @return Paginated refund overview with currency-specific approval analytics
     */
    fun getRefundsOverview(filter: RefundOverviewFilterDto): PageResponseDto<RefundOverviewResponseDto>

    /**
     * Get comprehensive system health overview.
     * 
     * Provides real-time system health metrics within a configurable time window.
     * Includes transaction throughput, refund processing efficiency, and merchant
     * activity status for operational monitoring.
     * 
     * **Metrics Included**:
     * - Transaction counts and status distribution within the window
     * - Current pending refunds + approvals/rejections within the window
     * - Active vs inactive merchant counts (current status)
     * 
     * @param windowHours Number of hours to look back from current time for metrics
     * @return Comprehensive system health metrics
     */
    fun getSystemHealthOverview(windowHours: Long): SystemHealthResponseDto
}