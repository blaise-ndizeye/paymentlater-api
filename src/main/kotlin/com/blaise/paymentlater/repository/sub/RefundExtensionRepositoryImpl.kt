package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enums.RefundStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import java.math.BigDecimal

class RefundExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : RefundExtensionRepository {

    override fun sumApprovedRefundsForPaymentIntent(paymentIntentId: ObjectId): BigDecimal {
        val aggregation = Aggregation.newAggregation(
            // Join refunds -> transactions
            Aggregation.lookup("transactions", "transactionId", "_id", "transaction"),

            // Flatten joined transactions array
            Aggregation.unwind("transaction"),

            // Match APPROVED refunds for the given payment intent
            Aggregation.match(
                Criteria.where("status").`is`(RefundStatus.APPROVED.name)
                    .and("transaction.paymentIntentId").isEqualTo(paymentIntentId)
            ),

            Aggregation.group()
                .sum(ConvertOperators.ToDecimal.toDecimal("\$amount"))
                .`as`("totalRefunded")
        )

        val result = mongoTemplate.aggregate(aggregation, "refunds", RefundSumResult::class.java)
            .mappedResults
            .firstOrNull()

        return result?.totalRefunded ?: BigDecimal.ZERO
    }

    data class RefundSumResult(val totalRefunded: BigDecimal)
}