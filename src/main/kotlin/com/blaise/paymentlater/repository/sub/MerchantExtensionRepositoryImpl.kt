package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.response.MerchantOverviewResponseDto
import com.blaise.paymentlater.dto.shared.BucketCount
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import com.blaise.paymentlater.dto.shared.MerchantOverviewFilter
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class MerchantExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : MerchantExtensionRepository {
    override fun search(
        filter: MerchantFilterDto,
        page: Int,
        size: Int
    ): Page<Merchant> {
        val (
            name,
            email,
            isActive,
            roles,
            createdStartDate,
            createdEndDate,
            updatedStartDate,
            updatedEndDate
        ) = filter

        val criteria = buildMongoCriteria {
            regex("name", name)
            regex("email", email)
            eq("isActive", isActive)
            `in`("roles", roles.map { it.name })

            gte("createdAt", createdStartDate)
            lte("createdAt", createdEndDate)
            gte("updatedAt", updatedStartDate)
            lte("updatedAt", updatedEndDate)
        }

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val query = Query().apply {
            criteria.forEach { addCriteria(it) }
            with(pageable)
        }

        val countQuery = Query().apply {
            criteria.forEach { addCriteria(it) }
        }

        val total = mongoTemplate.count(countQuery, Merchant::class.java)
        val list = mongoTemplate.find(query, Merchant::class.java)

        return PageImpl(list, pageable, total)
    }

    override fun getMerchantsOverview(filter: MerchantOverviewFilter): MerchantOverviewResponseDto {
        val matchCriteria = buildMongoCriteria {
            gte("createdAt", filter.createdStart)
            lte("createdAt", filter.createdEnd)
        }

        val totalQuery = Query()
        if (matchCriteria.isNotEmpty())
            totalQuery.addCriteria(Criteria().andOperator(*matchCriteria.toTypedArray()))
        val total = mongoTemplate.count(totalQuery, Merchant::class.java)

        val activeCriteria = matchCriteria.toMutableList()
        activeCriteria.add(Criteria.where("isActive").`is`(true))
        val activeQuery = Query(Criteria().andOperator(*activeCriteria.toTypedArray()))
        val active = mongoTemplate.count(activeQuery, Merchant::class.java)

        val inactive = total - active
        val activeRatio = if (total > 0) active.toDouble() / total else 0.0

        val aggregation = Aggregation.newAggregation(
            *matchCriteria.takeIf { it.isNotEmpty() }?.let {
                arrayOf(Aggregation.match(Criteria().andOperator(*it.toTypedArray())))
            } ?: emptyArray(),
            Aggregation.project()
                .andExpression("year(createdAt)").`as`("year")
                .andExpression("month(createdAt)").`as`("month"),
            Aggregation.group(Fields.fields("year", "month"))
                .count().`as`("count"),
            Aggregation.sort(Sort.Direction.ASC, "_id.year", "_id.month"),
            Aggregation.project()
                .and("_id.year").`as`("year")
                .and("_id.month").`as`("month")
                .and("count").`as`("count")
                .andExpression("dateToString('%Y-%m', dateFromParts(_id.year, _id.month, 1))").`as`("bucketStart")
        )
        val results = mongoTemplate.aggregate(
            aggregation,
            mongoTemplate.getCollectionName(Merchant::class.java),
            BucketCount::class.java
        )

        return MerchantOverviewResponseDto(
            total = total,
            active = active,
            inactive = inactive,
            activeRatio = activeRatio,
            createdTrend = results.mappedResults
        )
    }
}