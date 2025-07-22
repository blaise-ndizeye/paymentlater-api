package com.blaise.paymentlater.domain.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_tokens")
data class RefreshToken(
    @Id val id: ObjectId = ObjectId(),
    val userId: ObjectId,
    val token: String, // Hashed JWT
    @Indexed(expireAfter = "0s") val expiresAt: Instant,
    val createdBy: Instant = Instant.now()
)
