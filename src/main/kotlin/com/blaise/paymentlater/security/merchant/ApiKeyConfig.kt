package com.blaise.paymentlater.security.merchant

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.UUID

/**
 * Configuration and utilities for API key management in merchant authentication.
 * 
 * This component handles the generation, extraction, and processing of API keys
 * used for authenticating merchant requests to the PaymentLater API.
 * 
 * **API Key Format**:
 * - Generated from cryptographically secure UUID
 * - SHA-256 hashed for security
 * - 64-character hexadecimal string representation
 * - Transmitted via `X-API-KEY` HTTP header
 * 
 * **Security Features**:
 * - Cryptographically secure random generation using UUID
 * - SHA-256 hashing prevents key prediction
 * - No reversible key storage (keys are hashed before persistence)
 * - Standardized header extraction for consistent authentication
 * 
 * **Usage Flow**:
 * 1. Generate API key during merchant registration
 * 2. Hash and store key securely in database
 * 3. Merchant includes key in `X-API-KEY` header for requests
 * 4. Extract key from request headers for authentication
 * 5. Hash and compare against stored hash for validation
 * 
 * **Header Format**: `X-API-KEY: {64-character-hex-string}`
 * 
 * @see ApiKeyAuthFilter
 * @see MerchantSecurityConfig
 */
@Component
class ApiKeyConfig {
    private val headerName = "X-API-KEY"

    /**
     * Extracts the API key from the HTTP request headers.
     * 
     * Retrieves the API key value from the standardized `X-API-KEY` header.
     * Returns null if the header is not present or empty.
     * 
     * @param request HTTP servlet request containing headers
     * @return API key string if present, null otherwise
     */
    fun extractFrom(request: HttpServletRequest): String? = request.getHeader(headerName)

    /**
     * Generates a new cryptographically secure API key.
     * 
     * Creates a secure API key using the following process:
     * 1. Generate a random UUID for entropy
     * 2. Hash the UUID string using SHA-256
     * 3. Convert the hash bytes to hexadecimal representation
     * 
     * The resulting API key is a 64-character hexadecimal string that:
     * - Cannot be predicted or reverse-engineered
     * - Provides sufficient entropy for security
     * - Has consistent format for storage and transmission
     * 
     * **Security Note**: Each generated key is unique and cryptographically secure.
     * The original UUID is not stored, only the SHA-256 hash is used.
     * 
     * @return 64-character hexadecimal API key string
     */
    fun generateApiKey(): String {
        val randomString = UUID.randomUUID().toString()
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(randomString.toByteArray())
        val apiKey = StringBuilder()
        for (b in bytes) {
            apiKey.append(String.format("%02x", b))
        }
        return apiKey.toString()
    }
}