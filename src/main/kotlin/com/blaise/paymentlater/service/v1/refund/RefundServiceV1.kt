package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto

interface RefundServiceV1 {
    fun approveRefund(refundId: String): RefundTransactionResponseDto

    fun rejectRefund(refundId: String, body: RejectRefundRequestDto): RefundTransactionResponseDto

    fun getRefund(refundId: String, user: Any): RefundTransactionResponseDto

    fun findById(refundId: String): Refund
}