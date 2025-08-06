package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.dto.request.BillableItemRequestDto
import com.blaise.paymentlater.dto.request.PaymentMetadataRequestDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@Document("payment_intents")
data class PaymentIntent(
    @Id
    val id: ObjectId = ObjectId(),

    val merchantId: ObjectId,

    val items: List<BillableItemRequestDto>,

    val amount: BigDecimal,

    val currency: Currency,

    val status: PaymentStatus = PaymentStatus.PENDING,

    val metadata: PaymentMetadataRequestDto,

    val createdAt: Instant = Instant.now(),

    @Indexed(expireAfter = "0s")
    val expiresAt: Instant = Instant.now()
        .plus(Duration.ofDays(1))
)
