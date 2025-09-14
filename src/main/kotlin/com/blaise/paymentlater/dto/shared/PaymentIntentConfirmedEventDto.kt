package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Transaction

/**
 * Event DTO for payment intent confirmation notifications.
 * 
 * Published when a payment intent is successfully confirmed,
 * triggering webhook and email notifications to merchants.
 * 
 * Contains all necessary data for external communication:
 * - Merchant details for notification routing
 * - Payment intent information for business context
 * - Transaction details for confirmation specifics
 * 
 * @property merchant The merchant who owns the payment intent
 * @property paymentIntent The confirmed payment intent
 * @property transaction The transaction record created from confirmation
 */
data class PaymentIntentConfirmedEventDto(
    val merchant: Merchant,
    val paymentIntent: PaymentIntent,
    val transaction: Transaction
)
