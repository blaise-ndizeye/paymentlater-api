package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.RefundStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document("refunds")
@CompoundIndexes(
    CompoundIndex(
        name = "status_currency_requestedAt_approvedAt_rejectedAt_idx",
        def = "{'status': 1, 'currency': 1, 'requestedAt': -1, 'approvedAt': -1, 'rejectedAt': -1}",
    )
)
data class Refund(
    @Id
    val id: ObjectId = ObjectId(),

    val transactionId: ObjectId,

    @Indexed
    val status: RefundStatus,

    val reason: String,

    val rejectedReason: String? = null,

    val amount: BigDecimal,

    @Indexed
    val currency: Currency,

    @Indexed
    val requestedAt: Instant = Instant.now(),

    val approvedBy: String? = null,

    val rejectedBy: String? = null,

    @Indexed
    val approvedAt: Instant? = null,

    @Indexed
    val rejectedAt: Instant? = null,
)
