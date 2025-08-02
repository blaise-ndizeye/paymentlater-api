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
import org.springframework.web.server.ResponseStatusException
import java.net.SocketTimeoutException
import java.time.format.DateTimeParseException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponseDto> {
        val errors = e.bindingResult.allErrors.map { it.defaultMessage }
        return ResponseEntity.badRequest().body(
            ApiErrorResponseDto(
                errors.joinToString(", ")
            )
        )
    }

    @ExceptionHandler(EmailSendingException::class)
    fun handleEmailSending(e: EmailSendingException): ResponseEntity<ApiErrorResponseDto> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponseDto(
                    e.message ?: "Email sending failed"
                )
            )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(e: ResponseStatusException): ResponseEntity<ApiErrorResponseDto> {
        return ResponseEntity
            .status(e.statusCode)
            .body(
                ApiErrorResponseDto(
                    e.message
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
                message = "Service unavailable. Please try again later."
            )
        )
    }

    @ExceptionHandler(
        value = [
            IllegalArgumentException::class,
            DateTimeParseException::class
        ]
    )
    fun handleConversionErrors(e: Exception): ResponseEntity<ApiErrorResponseDto> {
        return ResponseEntity.badRequest().body(
            ApiErrorResponseDto(
                message = e.message ?: "Invalid request."
            )
        )
    }
}