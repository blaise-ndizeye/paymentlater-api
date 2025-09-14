package com.blaise.paymentlater.config

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

/**
 * Centralized hashing and encoding utilities for secure credential management.
 * 
 * This component provides secure hashing functions for different types of sensitive data
 * in the PaymentLater API, ensuring consistent security practices across the application.
 * 
 * **Supported Hashing Methods**:
 * - BCrypt for password hashing (adaptive cost, salt included)
 * - SHA-256 for token hashing (deterministic, fast)
 * - Base64 encoding for binary data representation
 * 
 * **Use Cases**:
 * - Admin user password hashing and verification
 * - Refresh token secure storage and validation
 * - API key hashing for merchant authentication
 * - Session token integrity verification
 * 
 * **Security Features**:
 * - BCrypt automatically handles salting and cost factors
 * - SHA-256 provides cryptographically secure hashing
 * - Base64 encoding ensures safe text representation
 * - Thread-safe operations for concurrent access
 * 
 * **BCrypt Configuration**:
 * - Uses default cost factor (currently 10)
 * - Each password gets unique salt automatically
 * - Resistant to rainbow table attacks
 * - Adaptive to increasing computational power
 * 
 * @see RefreshTokenService
 * @see AdminAuthServiceV1
 */
@Component
class HashEncoderConfig {
    private val bcrypt = BCryptPasswordEncoder()

    /**
     * Encodes a plain text password using BCrypt hashing algorithm.
     * 
     * BCrypt automatically generates a unique salt for each password and incorporates
     * it into the hash, making each encoded password unique even for identical inputs.
     * 
     * **Security Features**:
     * - Adaptive cost factor (default: 10)
     * - Automatic salt generation and inclusion
     * - Resistant to rainbow table attacks
     * - Time-constant comparison to prevent timing attacks
     * 
     * **Usage**: Primarily for admin user password storage during registration
     * and password updates.
     * 
     * @param password Plain text password to encode
     * @return BCrypt hash string containing salt and hash (60 characters)
     * @throws IllegalArgumentException if password is null
     */
    fun encode(password: String): String = bcrypt.encode(password)

    /**
     * Verifies a plain text password against a BCrypt hash.
     * 
     * Performs secure password verification by extracting the salt from the stored
     * hash and comparing it against the provided password using constant-time comparison.
     * 
     * **Security Features**:
     * - Time-constant comparison prevents timing attacks
     * - Automatic salt extraction from hash
     * - No hash reversal or password exposure
     * 
     * **Usage**: Used during admin authentication to verify login credentials.
     * 
     * @param password Plain text password to verify
     * @param hash BCrypt hash to compare against
     * @return true if password matches the hash, false otherwise
     * @throws IllegalArgumentException if either parameter is null
     */
    fun matches(password: String, hash: String): Boolean =
        bcrypt.matches(password, hash)

    /**
     * Creates a SHA-256 hash of a token and encodes it as Base64.
     * 
     * Generates a deterministic hash suitable for token storage and comparison.
     * Unlike BCrypt, this produces the same output for identical inputs, making
     * it suitable for token validation scenarios.
     * 
     * **Hash Process**:
     * 1. Convert token string to UTF-8 bytes
     * 2. Apply SHA-256 hashing algorithm
     * 3. Encode resulting bytes as Base64 string
     * 
     * **Use Cases**:
     * - Refresh token hashing for secure storage
     * - API key hashing for merchant authentication
     * - Session token integrity verification
     * 
     * **Security Note**: SHA-256 is cryptographically secure but deterministic.
     * Do not use for password hashing where salt is required.
     * 
     * @param token Token string to hash
     * @return Base64-encoded SHA-256 hash
     * @throws IllegalArgumentException if token is null
     */
    fun digest(token: String): String {
        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}