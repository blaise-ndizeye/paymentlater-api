package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enum.RefundStatus
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.response.RefundOverviewResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import com.blaise.paymentlater.dto.shared.RefundOverviewFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.MongoExpression
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

            // Flatten joined a transaction array
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

    /**
     * Get refunds overview with proper currency handling.
     * 
     * FINANCIAL INTEGRITY: Refunds are always grouped by currency to avoid calculation errors.
     * You cannot add USD + EUR + RWF amounts directly.
     * 
     * BEHAVIOR:
     * - Merchant with USD and EUR refunds = 2 separate rows (one per currency)
     * - This ensures financial calculations remain accurate
     * - Currency field is always shown for financial transparency
     * 
     * EXAMPLE:
     * Merchant A has: 5 USD refunds ($500) + 3 EUR refunds (€300)
     * Result: Row1(A, USD, 5 count, $500), Row2(A, EUR, 3 count, €300)
     */
    override fun getRefundsOverview(filter: RefundOverviewFilterDto): Page<RefundOverviewResponseDto> {
        val (start, end, merchantId, currencies, page, size) = filter

        // Build match criteria for fields BEFORE lookup
        val preMatchCriteria = buildMongoCriteria {
            gte("requestedAt", start)
            lte("requestedAt", end)
            `in`("currency", currencies?.map { currency -> currency.name })
        }

        // Build match criteria for fields AFTER lookup
        val postMatchCriteria = buildMongoCriteria {
            eq("paymentIntent.merchantId", merchantId)
        }

        // Lookup transaction for each refund
        val transactionLookup = Aggregation.lookup(
            "transactions",
            "transactionId",
            "_id",
            "transaction"
        )

        val unwindTransaction = Aggregation.unwind("transaction")

        // Lookup paymentIntent for each transaction
        val paymentIntentLookup = Aggregation.lookup(
            "payment_intents",
            "transaction.paymentIntentId",
            "_id",
            "paymentIntent"
        )

        val unwindPaymentIntent = Aggregation.unwind("paymentIntent")

        // Lookup merchant for each paymentIntent
        val merchantLookup = Aggregation.lookup(
            "merchants",
            "paymentIntent.merchantId",
            "_id",
            "merchant"
        )

        val unwindMerchant = Aggregation.unwind("merchant")

        // Add fields stage to convert BigDecimal amount using raw MongoDB expression
        val addFieldsStage = Aggregation.addFields()
            .addFieldWithValue(
                "amountAsDouble",
                AggregationExpression.from(
                    MongoExpression.create(
                        "{ ${'$'}cond: { if: { ${'$'}ne: ['${'$'}amount', null] }, then: { ${'$'}toDouble: '${'$'}amount' }, else: 0 } }"
                    )
                )
            )
            .build()

        // Group stage - ALWAYS group by currency to avoid financial calculation errors
        val group = Aggregation.group(
            Fields.fields("paymentIntent.merchantId", "merchant.name", "currency")
        )
            .first("paymentIntent.merchantId").`as`("merchantId")
            .first("merchant.name").`as`("merchantName")
            .first("currency").`as`("currency")
            .count().`as`("count")
            // Count approved refunds
            .sum(
                ConditionalOperators.`when`(
                    ComparisonOperators.valueOf("status").equalToValue("APPROVED")
                ).then(1).otherwise(0)
            ).`as`("approved")
            // Count rejected refunds
            .sum(
                ConditionalOperators.`when`(
                    ComparisonOperators.valueOf("status").equalToValue("REJECTED")
                ).then(1).otherwise(0)
            ).`as`("rejected")
            // Sum total refunded amount (only approved refunds contribute to this)
            .sum(
                AggregationExpression.from(
                    MongoExpression.create(
                        "{ ${'$'}cond: { if: { ${'$'}eq: ['${'$'}status', 'APPROVED'] }, then: '${'$'}amountAsDouble', else: 0 } }"
                    )
                )
            ).`as`("totalRefundedAmount")

        // Project stage - calculate approval rate and format fields
        val project = Aggregation.project()
            .and("merchantId").`as`("merchantId")
            .and("merchantName").`as`("merchantName")
            .and("currency").`as`("currency")
            .and("count").`as`("count")
            .and("approved").`as`("approved")
            .and("rejected").`as`("rejected")
            .and("totalRefundedAmount").`as`("totalRefundedAmount")
            // Calculate approval rate: approved / total count
            .and(
                AggregationExpression.from(
                    MongoExpression.create(
                        "{ ${'$'}cond: { if: { ${'$'}gt: ['${'$'}count', 0] }, then: { ${'$'}divide: [{ ${'$'}toDouble: '${'$'}approved' }, { ${'$'}toDouble: '${'$'}count' }] }, else: 0 } }"
                    )
                )
            ).`as`("approvedRate")

        val sort = Aggregation.sort(Sort.Direction.DESC, "totalRefundedAmount")
        val skip = Aggregation.skip(page.toLong() * size)
        val limit = Aggregation.limit(size.toLong())

        // Build the main aggregation pipeline
        val aggregationOperations = mutableListOf<AggregationOperation>().apply {
            // Apply pre-lookup filters first
            when {
                preMatchCriteria.isEmpty() -> {}
                preMatchCriteria.size == 1 -> add(Aggregation.match(preMatchCriteria.first()))
                else -> add(Aggregation.match(Criteria().andOperator(*preMatchCriteria.toTypedArray())))
            }

            add(transactionLookup)
            add(unwindTransaction)
            add(paymentIntentLookup)
            add(unwindPaymentIntent)
            add(merchantLookup)
            add(unwindMerchant)

            // Apply post-lookup filters
            when {
                postMatchCriteria.isEmpty() -> {} // no filter
                postMatchCriteria.size == 1 -> add(Aggregation.match(postMatchCriteria.first()))
                else -> add(Aggregation.match(Criteria().andOperator(*postMatchCriteria.toTypedArray())))
            }

            // Add BigDecimal conversion stage before grouping
            add(addFieldsStage)

            add(group)
            add(project)
            add(sort)
        }

        // Count total results before pagination
        val countAggregation = Aggregation.newAggregation(
            aggregationOperations + Aggregation.count().`as`("total")
        )

        val total = mongoTemplate.aggregate(
            countAggregation,
            "refunds",
            CountResult::class.java
        )
            .mappedResults
            .firstOrNull()
            ?.total ?: 0L

        // Add pagination to the main aggregation
        aggregationOperations.add(skip)
        aggregationOperations.add(limit)

        // Execute the main aggregation
        val results = mongoTemplate.aggregate(
            Aggregation.newAggregation(aggregationOperations),
            "refunds",
            RefundOverviewResponseDto::class.java
        ).mappedResults

        return PageImpl(
            results,
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "totalRefundedAmount")
            ),
            total
        )
    }

    data class CountResult(val total: Long)

    data class RefundSumResult(val totalRefunded: BigDecimal)
}