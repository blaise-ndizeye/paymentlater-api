package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.response.TransactionOverviewResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import com.blaise.paymentlater.dto.shared.TransactionOverviewFilterDto
import org.springframework.data.domain.Page

interface TransactionExtensionRepository {
    fun search(
        filter: TransactionFilterDto,
        page: Int,
        size: Int
    ): Page<Transaction>

    fun getTransactionOverview(filter: TransactionOverviewFilterDto): Page<TransactionOverviewResponseDto>
}