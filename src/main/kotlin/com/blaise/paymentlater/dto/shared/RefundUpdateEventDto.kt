package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.domain.model.Transaction

/**
 * Event DTO for refund status update notifications.
 * 
 * Published when a refund status changes (approved/rejected),
 * triggering appropriate merchant notifications.
 * 
 * Contains complete refund context for notification processing:
 * - Refund details with status and timestamps
 * - Associated payment intent for business context
 * - Original transaction for reference
 * - Merchant information for notification delivery
 * 
 * @property refund The refund with updated status
 * @property paymentIntent The associated payment intent
 * @property transaction The original transaction being refunded
 * @property merchant The merchant to notify
 */
data class RefundUpdateEventDto(
    val refund: Refund,
    val paymentIntent: PaymentIntent,
    val transaction: Transaction,
    val merchant: Merchant
)
