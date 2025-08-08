package com.blaise.paymentlater.repository.util

import org.springframework.data.mongodb.core.query.Criteria
import java.time.Instant
import java.util.Date

class MongoCriteriaDsl {
    private val criteriaList = mutableListOf<Criteria>()

    fun eq(field: String, value: Any?) {
        value?.let { criteriaList.add(Criteria.where(field).`is`(value)) }
    }

    fun `in`(field: String, values: Collection<*>?) {
        if (values != null && values.isNotEmpty())
            criteriaList.add(Criteria.where(field).`in`(values))
    }

    fun gte(field: String, value: Any?) {
        when (value) {
            is Instant -> criteriaList.add(Criteria.where(field).gte(Date.from(value)))
            else -> value?.let { criteriaList.add(Criteria.where(field).gte(it)) }
        }
    }

    fun lte(field: String, value: Any?) {
        when (value) {
            is Instant -> criteriaList.add(Criteria.where(field).lte(Date.from(value)))
            else -> value?.let { criteriaList.add(Criteria.where(field).lte(it)) }
        }
    }

    fun regex(field: String, value: String?) {
        value?.takeIf { it.isNotEmpty() }?.let {
            criteriaList.add(Criteria.where(field).regex(it, "i"))
        }
    }

    fun ne(field: String, value: Any?) {
        value?.let { criteriaList.add(Criteria.where(field).ne(value)) }
    }

    fun build(): List<Criteria> = criteriaList
}

fun buildMongoCriteria(block: MongoCriteriaDsl.() -> Unit): List<Criteria> = MongoCriteriaDsl().apply(block).build()