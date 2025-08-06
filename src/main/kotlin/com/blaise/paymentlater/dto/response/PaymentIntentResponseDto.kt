package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.dto.request.BillableItemRequestDto
import com.blaise.paymentlater.dto.request.PaymentMetadataRequestDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Payment intent response details")
data class PaymentIntentResponseDto(
    val id: String,

    val merchantId: String,

    val items: List<BillableItemRequestDto>,

    val amount: BigDecimal,

    val currency: Currency,

    val status: PaymentStatus,

    val metadata: PaymentMetadataRequestDto,

    val createdAt: String
)