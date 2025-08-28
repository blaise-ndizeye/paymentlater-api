package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import org.springframework.data.domain.Page

interface MerchantExtensionRepository {
    fun search(
        filter: MerchantFilterDto,
        page: Int,
        size: Int
    ): Page<Merchant>
}