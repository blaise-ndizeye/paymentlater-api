package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.response.RefundOverviewResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto
import com.blaise.paymentlater.dto.shared.RefundOverviewFilterDto
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

    fun getRefundsOverview(filter: RefundOverviewFilterDto): Page<RefundOverviewResponseDto>
}