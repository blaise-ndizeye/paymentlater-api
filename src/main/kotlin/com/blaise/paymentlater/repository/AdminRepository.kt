package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.repository.sub.AdminExtensionRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface AdminRepository : MongoRepository<Admin, ObjectId>, AdminExtensionRepository {
    fun findByUsername(username: String): Admin?

    fun existsByUsername(username: String): Boolean
}