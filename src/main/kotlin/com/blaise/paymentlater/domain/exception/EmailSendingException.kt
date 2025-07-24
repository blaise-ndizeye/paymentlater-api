package com.blaise.paymentlater.domain.exception

class EmailSendingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)