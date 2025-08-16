package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import java.math.BigDecimal

interface RefundExtensionRepository {
    fun sumApprovedRefundsForPaymentIntent(paymentIntentId: ObjectId): BigDecimal

    fun search(
        filter: RefundFilterDto,
        page: Int,
        size: Int
    ): Page<Refund>
}