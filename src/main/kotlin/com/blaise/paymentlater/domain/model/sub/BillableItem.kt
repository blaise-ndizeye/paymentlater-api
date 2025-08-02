package com.blaise.paymentlater.domain.model.sub

import java.math.BigDecimal

data class BillableItem(
    val id: String,
    val name: String,
    val description: String?,
    val unitAmount: BigDecimal,
    val quantity: Int = 1
)
