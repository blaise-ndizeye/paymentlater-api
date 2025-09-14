package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantRegisterResponseDto
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity

/**
 * Merchant authentication and API key management service.
 * 
 * Handles merchant lifecycle and API-based authentication:
 * 
 * **Core Features**:
 * - Merchant registration with automatic API key generation
 * - API key-based authentication for payment processing
 * - Secure API key regeneration with email notifications
 * - Webhook URL management for payment notifications
 * - Role-based access control integration
 * 
 * **Security Implementation**:
 * - API keys are cryptographically generated and securely hashed
 * - Double hashing: digest for lookup + bcrypt for verification
 * - Automatic email delivery of API keys to merchants
 * - Event-driven architecture for merchant registration
 * - Spring Security integration for authenticated merchant retrieval
 * 
 * **API Authentication Flow**:
 * 1. Merchant registers and receives API key via email
 * 2. API key is used for payment intent creation and confirmation
 * 3. Key can be regenerated for security purposes
 * 4. Webhook URLs configured for payment notifications
 */
interface MerchantAuthServiceV1 {
    
    /** Save merchant to repository */
    fun save(merchant: Merchant): Merchant

    /**
     * Register new merchant with API key generation.
     * 
     * Creates merchant account, generates secure API key,
     * sends welcome email, and publishes registration event.
     * 
     * @param body Merchant registration details
     * @return Merchant info with generated API key
     * @throws ResponseStatusException(400) if email already exists
     */
    fun register(body: MerchantRegisterRequestDto): MerchantRegisterResponseDto

    /** Find merchant by ID with role-based authorization */
    fun findById(id: ObjectId): Merchant

    /** Find merchant by email address */
    fun findByEmail(email: String): Merchant

    /**
     * Find merchant by API key with secure verification.
     * 
     * Uses digest for lookup and bcrypt for verification.
     * Returns null if key is invalid or merchant not found.
     */
    fun findByApiKeyDigest(apiKey: String): Merchant?

    /** Check if merchant email already exists */
    fun existsByEmail(email: String): Boolean

    /**
     * Get authenticated merchant from security context.
     * 
     * Used by protected endpoints to access current merchant.
     */
    fun getAuthenticatedMerchant(): Merchant

    /**
     * Regenerate API key for merchant with email notification.
     * 
     * Generates new API key, updates merchant, and sends
     * notification email with new key.
     */
    fun regenerateApiKey(email: String): ResponseEntity<Unit>

    /**
     * Set webhook URL for payment notifications.
     * 
     * Updates authenticated merchant's webhook URL
     * for receiving payment confirmations.
     */
    fun setWebhook(webhookUrl: String): ResponseEntity<Unit>
}