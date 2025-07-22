package com.blaise.paymentlater.security.admin

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

@Component
class HashEncoderConfig {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(password: String): String = bcrypt.encode(password)

    fun matches(password: String, hash: String): Boolean =
        bcrypt.matches(password, hash)

    fun hashLongString(token: String): String {
        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}