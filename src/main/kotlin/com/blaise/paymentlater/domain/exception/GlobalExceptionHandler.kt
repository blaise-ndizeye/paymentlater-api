package com.blaise.paymentlater.domain.exception

import com.blaise.paymentlater.dto.response.ApiErrorResponseDto
import com.mongodb.MongoSocketOpenException
import com.mongodb.MongoSocketReadTimeoutException
import com.mongodb.MongoTimeoutException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.SocketTimeoutException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map { it.defaultMessage }
        return ResponseEntity.badRequest().body(mapOf("errors" to errors))
    }

    @ExceptionHandler(EmailSendingException::class)
    fun handleEmailSending(e: EmailSendingException): ResponseEntity<ApiErrorResponseDto> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponseDto(
                    "EMAIL_SEND_FAILED",
                    e.message ?: "Email sending failed"
                )
            )
    }

    @ExceptionHandler(
        value = [
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            MongoSocketOpenException::class,
            SocketTimeoutException::class
        ]
    )
    fun handleMongoTimeouts(e: Exception): ResponseEntity<ApiErrorResponseDto> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ApiErrorResponseDto(
                code = HttpStatus.SERVICE_UNAVAILABLE.toString(),
                message = "Service unavailable. Please try again later."
            )
        )
    }
}