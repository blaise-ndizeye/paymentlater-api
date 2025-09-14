package com.blaise.paymentlater.service.v1.refreshtoken

import com.blaise.paymentlater.domain.model.RefreshToken
import com.blaise.paymentlater.repository.RefreshTokenRepository
import com.blaise.paymentlater.config.HashEncoderConfig
import com.blaise.paymentlater.security.admin.JwtConfig
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * JWT refresh token management service.
 * 
 * Manages the lifecycle of refresh tokens for secure authentication:
 * - Token creation with automatic expiration
 * - Secure token storage with hashing
 * - Token validation and cleanup
 * - One-token-per-user policy enforcement
 * 
 * **Security Features**:
 * - Tokens are hashed before storage (never stored in plaintext)
 * - Automatic expiration based on configuration
 * - Single active refresh token per user (prevents token accumulation)
 * - Secure cleanup operations
 * 
 * **Usage**: Used by authentication services to maintain user sessions
 * without requiring frequent password re-entry.
 */
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hashEncoderConfig: HashEncoderConfig,
    private val jwtConfig: JwtConfig,
) {
    
    /**
     * Save refresh token for user (replacing any existing token).
     * 
     * Implements one-token-per-user policy by deleting existing tokens
     * before creating new one. Token is hashed before storage.
     */
    fun saveRefreshToken(userId: ObjectId, refreshToken: String) {
        deleteByUserId(userId)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                token = hashEncoderConfig.digest(refreshToken),
                expiresAt = Instant.now().plusMillis(
                    jwtConfig.refreshTokenValidityMillis
                )
            )
        )
    }

    /** Find refresh token by user ID and token value */
    fun findByUserIdAndToken(userId: ObjectId, token: String): RefreshToken? =
        refreshTokenRepository.findByUserIdAndToken(userId, token)

    /** Delete specific refresh token for user */
    fun deleteByUserIdAndToken(userId: ObjectId, token: String) =
        refreshTokenRepository.deleteByUserIdAndToken(userId, token)

    /** Delete all refresh tokens for user (used during logout) */
    fun deleteByUserId(userId: ObjectId) = refreshTokenRepository.deleteByUserId(userId)
}