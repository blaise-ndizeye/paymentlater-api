package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.repository.sub.PaymentIntentExtensionRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface PaymentIntentRepository : MongoRepository<PaymentIntent, ObjectId>, PaymentIntentExtensionRepository {
    fun findByMerchantId(merchantId: ObjectId): PaymentIntent?

    fun findByMerchantIdAndStatus(merchantId: ObjectId, status: PaymentStatus): PaymentIntent?

    fun findByMerchantIdAndExpiresAtAfter(merchantId: ObjectId, after: Instant): PaymentIntent?
}