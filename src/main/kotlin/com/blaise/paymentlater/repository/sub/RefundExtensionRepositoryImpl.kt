package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
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

    override fun search(
        filter: RefundFilterDto,
        page: Int,
        size: Int
    ): Page<Refund> {
        val (
            merchantId,
            approvedDateStart,
            approvedDateEnd,
            rejectedDateStart,
            rejectedDateEnd,
            statuses,
            currencies
        ) = filter

        val matchCriteria = buildMongoCriteria {
            `in`("status", statuses?.map { it.name })
            `in`("currency", currencies?.map { it.name })
            gte("approvedAt", approvedDateStart)
            lte("approvedAt", approvedDateEnd)
            gte("rejectedAt", rejectedDateStart)
            lte("rejectedAt", rejectedDateEnd)
        }

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "requestedAt")
        )

        val aggregationOps = mutableListOf<AggregationOperation>()

        // 1. Lookup transaction for each refund
        aggregationOps.add(
            Aggregation.lookup("transactions", "transactionId", "_id", "transaction")
        )

        // 2. Unwind transaction
        aggregationOps.add(Aggregation.unwind("transaction"))

        // 3. Lookup paymentIntent for each transaction
        aggregationOps.add(
            Aggregation.lookup("payment_intents", "transaction.paymentIntentId", "_id", "paymentIntent")
        )

        // 4. Unwind paymentIntent
        aggregationOps.add(Aggregation.unwind("paymentIntent"))

        // 5. Apply match criteria (including merchantId now that we have paymentIntent)
        val allCriteria = mutableListOf<Criteria>()
        allCriteria.addAll(matchCriteria)

        if (merchantId != null) {
            allCriteria.add(Criteria.where("paymentIntent.merchantId").`is`(merchantId))
        }

        if (allCriteria.isNotEmpty()) {
            aggregationOps.add(Aggregation.match(Criteria().andOperator(*allCriteria.toTypedArray())))
        }

        // 6. Sort
        aggregationOps.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")))

        // 7. Pagination
        aggregationOps.add(Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong()))
        aggregationOps.add(Aggregation.limit(pageable.pageSize.toLong()))

        val aggregation = Aggregation.newAggregation(aggregationOps)

        val results = mongoTemplate.aggregate(aggregation, "refunds", Refund::class.java)
        val list = results.mappedResults

        // For total count without pagination
        val countAggregation = Aggregation.newAggregation(
            aggregationOps.filterNot { it is SkipOperation || it is LimitOperation }
                .plus(Aggregation.count().`as`("total"))
        )
        val countResults = mongoTemplate.aggregate(countAggregation, "refunds", CountResult::class.java)
        val total = countResults.mappedResults.firstOrNull()?.total ?: 0L

        return PageImpl(list, pageable, total)
    }

    data class CountResult(val total: Long)

    data class RefundSumResult(val totalRefunded: BigDecimal)
}