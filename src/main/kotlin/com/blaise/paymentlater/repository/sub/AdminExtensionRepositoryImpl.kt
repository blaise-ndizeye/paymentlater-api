package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.dto.response.SystemHealthResponseDto
import com.blaise.paymentlater.dto.shared.MerchantHealth
import com.blaise.paymentlater.dto.shared.RefundHealth
import com.blaise.paymentlater.dto.shared.TransactionHealth
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

class AdminExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : AdminExtensionRepository {

    /**
     * Get a comprehensive system health overview within the specified time window.
     *
     * The windowHours parameter represents a look-back period from now() to gather metrics:
     * - Transaction counts and status distribution within the window
     * - Refund activity (approvals/rejections) within the window + current pending count
     * - Current merchant active/inactive status (not time-bound)
     *
     * @param windowHours Number of hours to look back from current time
     * @return SystemHealthResponseDto containing comprehensive health metrics
     */
    override fun getSystemHealthOverview(windowHours: Long): SystemHealthResponseDto {
        val windowStart = Instant.now().minusSeconds(windowHours * 3600)

        val transactionHealth = getTransactionHealth(windowStart)
        val refundHealth = getRefundHealth(windowStart)
        val merchantHealth = getMerchantHealth()

        return SystemHealthResponseDto(
            windowHours = windowHours,
            transactions = transactionHealth,
            refunds = refundHealth,
            merchants = merchantHealth
        )
    }

    /**
     * Get transaction health metrics within the time window.
     */
    private fun getTransactionHealth(windowStart: Instant): TransactionHealth {
        val aggregation = Aggregation.newAggregation(
            // Match transactions within the time window
            Aggregation.match(
                Criteria.where("confirmedAt").gte(windowStart)
            ),

            // Group by status to get counts
            Aggregation.group("status")
                .count().`as`("count")
                .first("status").`as`("status"),

            // Project to format output
            Aggregation.project()
                .and("_id").`as`("status")
                .and("count").`as`("count")
        )

        val results = mongoTemplate.aggregate(
            aggregation,
            "transactions",
            TransactionStatusCount::class.java
        ).mappedResults

        val byStatus = results.associate { it.status to it.count }
        val total = results.sumOf { it.count }

        return TransactionHealth(
            total = total,
            byStatus = byStatus
        )
    }

    /**
     * Get refund health metrics.
     */
    private fun getRefundHealth(windowStart: Instant): RefundHealth {
        // Count current pending refunds (not time-bound)
        val pendingCount = mongoTemplate.count(
            Query(Criteria.where("status").`is`("PENDING")),
            "refunds"
        )

        // Count refunds approved within the window
        val approvedInWindow = mongoTemplate.count(
            Query(
                Criteria.where("status").`is`("APPROVED")
                    .and("approvedAt").gte(windowStart)
            ),
            "refunds"
        )

        // Count refunds rejected within the window
        val rejectedInWindow = mongoTemplate.count(
            Query(
                Criteria.where("status").`is`("REJECTED")
                    .and("rejectedAt").gte(windowStart)
            ),
            "refunds"
        )

        return RefundHealth(
            pending = pendingCount,
            approvedLastWindow = approvedInWindow,
            rejectedLastWindow = rejectedInWindow
        )
    }

    /**
     * Get merchant health metrics (current status, not time-bound).
     */
    private fun getMerchantHealth(): MerchantHealth {
        val activeCount = mongoTemplate.count(
            Query(Criteria.where("isActive").`is`(true)),
            "merchants"
        )

        val inactiveCount = mongoTemplate.count(
            Query(Criteria.where("isActive").`is`(false)),
            "merchants"
        )

        return MerchantHealth(
            active = activeCount,
            inactive = inactiveCount
        )
    }
    
    private data class TransactionStatusCount(
        val status: String,
        val count: Long
    )
}
