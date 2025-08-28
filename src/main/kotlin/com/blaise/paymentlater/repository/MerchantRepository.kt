package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.repository.sub.MerchantExtensionRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface MerchantRepository : MongoRepository<Merchant, ObjectId>, MerchantExtensionRepository {
    fun findByEmail(email: String): Merchant?

    fun findByApiKeyDigest(apiKey: String): Merchant?

    fun existsByEmail(email: String): Boolean
}