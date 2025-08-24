package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.PaymentStatus
import com.blaise.paymentlater.dto.request.BillableItemRequestDto
import com.blaise.paymentlater.dto.request.PaymentMetadataRequestDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@Document("payment_intents")
@CompoundIndexes(
    CompoundIndex(
        name = "currency_status_createdAt_idx",
        def = "{'currency': 1, 'status': 1, 'createdAt': -1}",
    )
)
data class PaymentIntent(
    @Id
    val id: ObjectId = ObjectId(),

    val merchantId: ObjectId,

    val items: List<BillableItemRequestDto>,

    val amount: BigDecimal,

    @Indexed
    val currency: Currency,

    @Indexed
    val status: PaymentStatus = PaymentStatus.PENDING,

    val metadata: PaymentMetadataRequestDto,

    @Indexed
    val createdAt: Instant = Instant.now(),

//    @Indexed(expireAfter = "0s")
    val expiresAt: Instant = Instant.now()
        .plus(Duration.ofHours(2)),

    val cancelledAt: Instant? = null,

    val cancelledBy: ObjectId? = null
)
