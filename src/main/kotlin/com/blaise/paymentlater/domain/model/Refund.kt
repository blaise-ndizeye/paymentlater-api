package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.RefundStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document("refunds")
data class Refund(
    @Id
    val id: ObjectId = ObjectId(),

    val transactionId: ObjectId,

    val status: RefundStatus,

    val reason: String,

    val amount: BigDecimal,

    val currency: Currency,

    val requestedAt: Instant = Instant.now(),

    val approvedAt: Instant? = null,

    val rejectedAt: Instant? = null,
)
