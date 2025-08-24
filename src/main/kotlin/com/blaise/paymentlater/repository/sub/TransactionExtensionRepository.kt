package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import org.springframework.data.domain.Page

interface TransactionExtensionRepository {
    fun search(
        filter: TransactionFilterDto,
        page: Int,
        size: Int
    ): Page<Transaction>
}