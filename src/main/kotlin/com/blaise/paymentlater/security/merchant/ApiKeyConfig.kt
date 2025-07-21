package com.blaise.paymentlater.security.merchant

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.UUID

@Component
class ApiKeyConfig {
    private val headerName = "X-API-KEY"

    fun extractFrom(request: HttpServletRequest): String? = request.getHeader(headerName)

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