package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    private data class CountResult(val total: Long)
}



