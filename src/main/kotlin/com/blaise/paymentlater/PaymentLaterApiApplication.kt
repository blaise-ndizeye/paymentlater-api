package com.blaise.paymentlater

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class PaymentLaterApiApplication

fun main(args: Array<String>) {
    runApplication<PaymentLaterApiApplication>(*args)
}
