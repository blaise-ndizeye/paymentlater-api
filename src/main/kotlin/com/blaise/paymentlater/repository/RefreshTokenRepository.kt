package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.RefreshToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepository : MongoRepository<RefreshToken, ObjectId> {
    fun findByUserIdAndToken(userId: ObjectId, token: String): RefreshToken?

    fun deleteByUserIdAndToken(userId: ObjectId, token: String)

    fun deleteByUserId(userId: ObjectId)
}