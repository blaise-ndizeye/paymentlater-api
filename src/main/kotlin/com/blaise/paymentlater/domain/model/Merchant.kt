package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enum.UserRole
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("merchants")
data class Merchant(
    @Id
    val id: ObjectId = ObjectId(),

    val name: String,

    @Indexed(unique = true)
    val email: String,

    @Indexed(unique = true)
    val apiKey: String,

    @Indexed(unique = true)
    val apiKeyDigest: String,

    val webhookUrl: String? = null,

    val isActive: Boolean = true,

    val roles: Set<UserRole> = setOf(UserRole.MERCHANT),

    val createdAt: Instant = Instant.now()
)
