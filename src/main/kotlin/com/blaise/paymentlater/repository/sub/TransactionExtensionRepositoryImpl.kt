package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.MongoExpression
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria

class TransactionExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : TransactionExtensionRepository {

    override fun search(
        filter: TransactionFilterDto,
        page: Int,
        size: Int
    ): Page<Transaction> {
        val (currencies, statuses, start, end, merchantId) = filter

        // Build criteria using your DSL
        val criteriaList = buildMongoCriteria {
            `in`("currency", currencies?.map { it.name })
            `in`("status", statuses?.map { it.name })
            gte("confirmedAt", start)
            lte("confirmedAt", end)
        }.toMutableList()

        // Add merchantId filter (joins through paymentIntent)
        merchantId?.let { criteriaList.add(Criteria("paymentIntent.merchantId").`is`(it)) }

        // Avoid empty $and
        val matchCriteria = when {
            criteriaList.isEmpty() -> Criteria() // no filter
            criteriaList.size == 1 -> criteriaList.first()
            else -> Criteria().andOperator(*criteriaList.toTypedArray())
        }

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "confirmedAt")
        )

        // Count aggregation
        val countAggregation = Aggregation.newAggregation(
            Aggregation.lookup(
                "payment_intents",
                "paymentIntentId",
                "_id",
                "paymentIntent"
            ),
            Aggregation.unwind("paymentIntent"),
            Aggregation.match(matchCriteria),
            Aggregation.count().`as`("total")
        )

        val total = mongoTemplate.aggregate(
            countAggregation,
            "transactions",
            CountResult::class.java
        )
            .mappedResults
            .firstOrNull()
            ?.total ?: 0L

        // Main aggregation with pagination
        val aggregation = Aggregation.newAggregation(
            Aggregation.lookup(
                "payment_intents",
                "paymentIntentId",
                "_id",
                "paymentIntent"
            ),
            Aggregation.unwind("paymentIntent"),
            Aggregation.match(matchCriteria),
            Aggregation.sort(pageable.sort),
            Aggregation.skip(pageable.offset),
            Aggregation.limit(pageable.pageSize.toLong())
        )

        val list = mongoTemplate.aggregate(
            aggregation,
            "transactions",
            Transaction::class.java
        )
            .mappedResults

        return PageImpl(list, pageable, total)
    }

    /**
     * Get a transaction overview with proper currency handling.
     * 
     * FINANCIAL INTEGRITY: To avoid calculation errors, transactions are ALWAYS grouped 
     * by currency internally. You cannot add USD + EUR + RWF amounts directly.
     * 
     * When groupByCurrency = true: Returns separate rows per currency, currency field visible
     * When groupByCurrency = false: Returns separate rows per currency, currency field visible
     *                               (currency is always shown for financial transparency)
     * 
     * BEHAVIOR:
     * - Merchant with USD and EUR transactions = 2 separate rows (even if groupByCurrency=false)
     * - This ensures financial calculations remain accurate
     * - Currency field is always shown to maintain transparency about what currency each row represents
     * 
     * EXAMPLE:
     * Merchant A has: 100 USD + 50 EUR transactions
     * groupByCurrency=true:  Row1(A, USD, $100), Row2(A, EUR, €50) 
     * groupByCurrency=false: Row1(A, USD, $100), Row2(A, EUR, €50) - same result!
     */
    override fun getTransactionOverview(filter: TransactionOverviewFilterDto): Page<TransactionOverviewResponseDto> {
        val (startDate, endDate, merchantId, statuses, currencies, page, size) = filter

        // Build match criteria for fields BEFORE lookup
        val preMatchCriteria = buildMongoCriteria {
            gte("confirmedAt", startDate)
            lte("confirmedAt", endDate)
            `in`("status", statuses?.map { status -> status.name })
            `in`("currency", currencies?.map { currency -> currency.name })
        }

        // Build match criteria for fields AFTER lookup
        val postMatchCriteria = buildMongoCriteria {
            eq("paymentIntent.merchantId", merchantId)
        }

        // First lookup to join with payment_intents
        val paymentIntentLookup = Aggregation.lookup(
            "payment_intents",
            "paymentIntentId",
            "_id",
            "paymentIntent"
        )

        val unwindPaymentIntent = Aggregation.unwind("paymentIntent")

        // Second lookup to join with a merchant collection
        val merchantLookup = Aggregation.lookup(
            "merchants",
            "paymentIntent.merchantId",
            "_id",
            "merchant"
        )

        val unwindMerchant = Aggregation.unwind("merchant")

        // Add fields' stage to convert BigDecimal amount using raw MongoDB expression
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
        // Both groupByCurrency true/false scenarios group by currency for data integrity
        val group = Aggregation.group(
            Fields.fields("paymentIntent.merchantId", "merchant.name", "currency")
        )
            .first("paymentIntent.merchantId").`as`("merchantId")
            .first("merchant.name").`as`("merchantName")
            .first("currency").`as`("currency")
            .count().`as`("count")
            .sum("amountAsDouble").`as`("totalAmount")
            // Count successful transactions (SUCCESS or REFUNDED)
            .sum(
                ConditionalOperators.`when`(
                    ComparisonOperators.valueOf("status").equalToValue("SUCCESS")
                ).then(1).otherwise(0)
            ).`as`("successCount")
            .sum(
                ConditionalOperators.`when`(
                    ComparisonOperators.valueOf("status").equalToValue("REFUNDED")
                ).then(1).otherwise(0)
            ).`as`("refundedCount")

        // Project stage - simplified since currency is always shown for financial transparency
        val project = Aggregation.project()
            .and("merchantId").`as`("merchantId")
            .and("merchantName").`as`("merchantName")
            .and("currency").`as`("currency")
            .and("count").`as`("count")
            .and("totalAmount").`as`("totalAmount")
            .and("successCount").plus("refundedCount").`as`("succeeded")
            .and(
                AggregationExpression.from(
                    MongoExpression.create(
                        "{ ${'$'}divide: [{ ${'$'}toDouble: { ${'$'}add: ['${'$'}successCount', '${'$'}refundedCount'] } }, { ${'$'}toDouble: '${'$'}count' }] }"
                    )
                )
            ).`as`("successRate")
            .and("totalAmount").divide("count").`as`("avgOrderValue")

        val sort = Aggregation.sort(Sort.Direction.DESC, "totalAmount")
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
            "transactions",
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
            "transactions",
            TransactionOverviewResponseDto::class.java
        ).mappedResults

        return PageImpl(
            results,
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "totalAmount")
            ),
            total
        )
    }

    private data class CountResult(val total: Long)
}



