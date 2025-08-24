package com.blaise.paymentlater.dto.response

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.PaymentMethod
import com.blaise.paymentlater.domain.enum.TransactionStatus
import com.blaise.paymentlater.dto.request.TransactionMetadataRequestDto
import java.math.BigDecimal

data class TransactionResponseDto(
    val id: String,
    val paymentIntentId: String,
    val parentTransactionId: String?,
    val amount: BigDecimal,
    val currency: Currency,
    val paymentMethod: PaymentMethod,
    val status: TransactionStatus,
    val confirmedAt: String?,
    val metadata: TransactionMetadataRequestDto
)
