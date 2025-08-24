package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.enum.PaymentStatus
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

class PaymentIntentExtensionRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : PaymentIntentExtensionRepository {

    override fun search(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): Page<PaymentIntent> {
        val (merchantId, start, end, statuses, currencies) = filter

        val criteria = buildMongoCriteria {
            eq("merchantId", merchantId)
            `in`("status", statuses?.map { it.name })
            `in`("currency", currencies?.map { it.name })
            gte("createdAt", start)
            lte("createdAt", end)
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

        val total = mongoTemplate.count(
            query.skip(-1).limit(-1),
            PaymentIntent::class.java
        )

        val list = mongoTemplate.find(query, PaymentIntent::class.java)

        return PageImpl(list, pageable, total)
    }

    override fun findPendingWithExpiredAtBefore(now: Instant): List<PaymentIntent> {
        val criteria = buildMongoCriteria {
            eq("status", PaymentStatus.PENDING.name)
            lte("expiresAt", now)
        }

        val query = Query().apply {
            criteria.forEach { addCriteria(it) }
        }

        return mongoTemplate.find(query, PaymentIntent::class.java)
    }
}