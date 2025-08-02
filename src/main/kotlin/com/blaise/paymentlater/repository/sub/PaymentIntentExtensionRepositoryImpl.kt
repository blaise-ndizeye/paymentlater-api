package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.model.PaymentIntent
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

class PaymentIntentExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : PaymentIntentExtensionRepository {

    override fun findByAdminFilters(
        statuses: List<PaymentStatus>?,
        currencies: List<String>?,
        start: Instant?,
        end: Instant?,
        pageable: Pageable
    ): Page<PaymentIntent> {
        val criteria = mutableListOf<Criteria>()

        statuses?.takeIf { it.isNotEmpty() }?.let {
            criteria.add(Criteria.where("status").`in`(*it.toTypedArray()))
        }

        currencies?.takeIf { it.isNotEmpty() }?.let {
            criteria.add(Criteria.where("currency").`in`(*it.toTypedArray()))
        }

        if (start != null && end != null)
            criteria.add(Criteria.where("createdAt").gte(start).lte(end))

        val query = if (criteria.isEmpty())
            Query()
        else {
            Query()
                .addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
        }.with(pageable)

        val total = mongoTemplate.count(
            query.skip(-1).limit(-1),
            PaymentIntent::class.java
        )
        val list = mongoTemplate.find(query, PaymentIntent::class.java)

        return PageImpl(list, pageable, total)
    }
}