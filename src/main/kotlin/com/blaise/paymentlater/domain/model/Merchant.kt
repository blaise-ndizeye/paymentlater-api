package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enums.Role
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

    val webhookUrl: String? = null,

    val isActive: Boolean = true,

    val roles: Set<Role> = setOf(Role.MERCHANT),

    val createdAt: Instant = Instant.now()
)
