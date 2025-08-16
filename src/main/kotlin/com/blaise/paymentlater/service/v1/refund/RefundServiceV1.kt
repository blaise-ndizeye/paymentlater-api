package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto

interface RefundServiceV1 {
    fun approveRefund(refundId: String): RefundTransactionResponseDto

    fun rejectRefund(refundId: String, body: RejectRefundRequestDto): RefundTransactionResponseDto

    fun getRefund(refundId: String, user: Any): RefundTransactionResponseDto

    fun getRefunds(
        filter: RefundFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<RefundTransactionResponseDto>

    fun findById(refundId: String): Refund
}