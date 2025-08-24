package com.blaise.paymentlater.domain.model

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.PaymentMethod
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.dto.request.TransactionMetadataRequestDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document("transactions")
data class Transaction(
    @Id
    val id: ObjectId = ObjectId(),

    val paymentIntentId: ObjectId,

    val parentTransactionId: ObjectId? = null, // For partial refunds to link to the original transaction

    val amount: BigDecimal,

    val currency: Currency,

    val paymentMethod: PaymentMethod,

    val status: TransactionStatus,

    val confirmedAt: Instant = Instant.now(),

    val metadata: TransactionMetadataRequestDto,
)
