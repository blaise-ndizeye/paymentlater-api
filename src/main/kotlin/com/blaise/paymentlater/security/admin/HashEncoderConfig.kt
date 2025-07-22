package com.blaise.paymentlater.security.admin

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class HashEncoderConfig {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(password: String): String = bcrypt.encode(password)

    fun matches(password: String, hash: String): Boolean =
        bcrypt.matches(password, hash)

}