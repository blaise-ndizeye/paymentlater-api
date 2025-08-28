package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import com.blaise.paymentlater.repository.util.buildMongoCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
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
}