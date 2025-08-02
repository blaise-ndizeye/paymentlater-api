package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.model.sub.BillableItem
import com.blaise.paymentlater.domain.model.sub.PaymentMetadata
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Payment intent response details")
data class PaymentIntentResponseDto(
    val id: String,

    val merchantId: String,

    val items: List<BillableItem>,

    val amount: BigDecimal,

    val currency: Currency,

    val status: PaymentStatus,

    val metadata: PaymentMetadata,

    val createdAt: String
)