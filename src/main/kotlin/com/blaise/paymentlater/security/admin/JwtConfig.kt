package com.blaise.paymentlater.security.admin

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.Base64
import java.util.Date

@Component
class JwtConfig(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    private enum class TokenType {
        ACCESS, REFRESH
    }

    private val headerName = "Authorization"
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))
    private val accessTokenValidityMillis = 15L * 60L * 1000L // 15 minutes
    val refreshTokenValidityMillis = 30L * 24L * 60L * 60L * 1000L // 30 days

    private fun generateToken(username: String, tokenType: TokenType, validity: Long): String {
        val now = Date()
        val expiration = Date(now.time + validity)
        return Jwts.builder()
            .subject(username)
            .claim("type", tokenType.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? = try {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token

        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(rawToken)
            .payload
    } catch (_: Exception) {
        null
    }

    fun extractFrom(request: HttpServletRequest): String? = request.getHeader(headerName)

    fun generateAccessToken(username: String): String =
        generateToken(username, TokenType.ACCESS, accessTokenValidityMillis)

    fun generateRefreshToken(username: String): String =
        generateToken(username, TokenType.REFRESH, refreshTokenValidityMillis)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == TokenType.ACCESS.name
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == TokenType.REFRESH.name
    }

    fun getUsernameFromToken(token: String): String {
        val claims = parseAllClaims(token) ?: throw ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid token"
        )
        return claims.subject
    }
}