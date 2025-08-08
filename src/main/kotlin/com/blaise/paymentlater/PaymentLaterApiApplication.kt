package com.blaise.paymentlater

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableRetry
@EnableScheduling
class PaymentLaterApiApplication

fun main(args: Array<String>) {
    runApplication<PaymentLaterApiApplication>(*args)
}
