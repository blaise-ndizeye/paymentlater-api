package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enum.UserRole
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("admins")
data class Admin(
    @Id
    val id: ObjectId = ObjectId(),

    @Indexed(unique = true)
    val username: String,

    val password: String,

    val roles: Set<UserRole> = setOf(UserRole.ADMIN),

    val createdAt: Instant = Instant.now()
)
