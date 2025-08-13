package com.blaise.paymentlater.repository.sub

import org.bson.types.ObjectId
import java.math.BigDecimal

interface RefundExtensionRepository {
    fun sumApprovedRefundsForPaymentIntent(paymentIntentId: ObjectId): BigDecimal
}