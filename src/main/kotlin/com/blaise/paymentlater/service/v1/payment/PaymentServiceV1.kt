package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.request.ConfirmPaymentIntentRequestDto
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import java.time.Instant

/**
 * Core payment processing service for PaymentLater API.
 * 
 * Handles the complete payment lifecycle from intent creation through confirmation:
 * 
 * **Payment Flow**:
 * 1. Create payment intent with items and metadata
 * 2. Merchant processes payment through various methods
 * 3. Confirm payment intent (success or failure)
 * 4. Automatic webhook notification and transaction recording
 * 
 * **Key Features**:
 * - Multi-currency support (USD, EUR, RWF)
 * - Comprehensive authorization and validation
 * - Automatic payment intent expiration
 * - Event-driven architecture with webhook notifications
 * - Transaction history and audit trails
 * - Role-based access control (merchants and admins)
 * 
 * **Security**: All operations include proper authorization checks to ensure
 * merchants can only access their own payment intents and transactions.
 */
interface PaymentServiceV1 {
    
    /** Save payment intent to repository */
    fun save(paymentIntent: PaymentIntent): PaymentIntent

    /** Get paginated payment intents with filtering support */
    fun getPayments(
        filter: PaymentIntentFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto>

    /** Get single payment intent with role-based authorization */
    fun getPayment(paymentIntentId: String, user: Any): PaymentIntentResponseDto

    /** Find payment intent by ID or throw 404 exception */
    fun findById(id: String): PaymentIntent

    /**
     * Create new payment intent for authenticated merchant.
     * Automatically calculates total amount from items.
     */
    fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto

    /** Batch expire old pending payment intents (scheduled job) */
    fun expireOldPaymentIntents(now: Instant)

    /** Cancel pending payment intent with authorization checks */
    fun cancelPaymentIntent(paymentIntentId: String, user: Any): PaymentIntentResponseDto

    /**
     * Confirm payment intent and create transaction record.
     * Publishes webhook event for merchant notification.
     */
    fun confirmPaymentIntent(paymentIntentId: String, body: ConfirmPaymentIntentRequestDto): PaymentIntentResponseDto
}