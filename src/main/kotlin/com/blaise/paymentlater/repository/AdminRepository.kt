package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Admin
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface AdminRepository : MongoRepository<Admin, ObjectId> {
    fun findByUsername(username: String): Admin?

    fun existsByUsername(username: String): Boolean
}