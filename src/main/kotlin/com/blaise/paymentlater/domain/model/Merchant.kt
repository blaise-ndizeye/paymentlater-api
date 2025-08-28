package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enum.UserRole
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("merchants")
@CompoundIndexes(
    CompoundIndex(
        name = "name_email_isActive_roles_createdAt_updatedAt",
        def = "{name: 1, email: 1, isActive: 1, roles: 1, createdAt: -1, updatedAt: -1}"
    )
)
data class Merchant(
    @Id
    val id: ObjectId = ObjectId(),

    @Indexed
    val name: String,

    @Indexed(unique = true)
    val email: String,

    @Indexed(unique = true)
    val apiKey: String,

    @Indexed(unique = true)
    val apiKeyDigest: String,

    val webhookUrl: String? = null,

    @Indexed
    val isActive: Boolean = true,

    @Indexed
    val roles: Set<UserRole> = setOf(UserRole.MERCHANT),

    @Indexed
    val createdAt: Instant = Instant.now(),

    @Indexed
    val updatedAt: Instant = Instant.now()
)
