package com.blaise.paymentlater.service.v1.refreshtoken

import com.blaise.paymentlater.domain.model.RefreshToken
import com.blaise.paymentlater.repository.RefreshTokenRepository
import com.blaise.paymentlater.security.admin.HashEncoderConfig
import com.blaise.paymentlater.security.admin.JwtConfig
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hashEncoderConfig: HashEncoderConfig,
    private val jwtConfig: JwtConfig,
) {
    fun saveRefreshToken(userId: ObjectId, refreshToken: String) {
        deleteByUserId(userId)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                token = hashEncoderConfig.hashLongString(refreshToken),
                expiresAt = Instant.now().plusMillis(
                    jwtConfig.refreshTokenValidityMillis
                )
            )
        )
    }

    fun findByUserIdAndToken(userId: ObjectId, token: String): RefreshToken? =
        refreshTokenRepository.findByUserIdAndToken(userId, token)

    fun deleteByUserIdAndToken(userId: ObjectId, token: String) =
        refreshTokenRepository.deleteByUserIdAndToken(userId, token)

    fun deleteByUserId(userId: ObjectId) = refreshTokenRepository.deleteByUserId(userId)
}