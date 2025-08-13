package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto

interface RefundServiceV1 {
    fun approveRefund(refundId: String): RefundTransactionResponseDto

    fun findById(refundId: String): Refund
}