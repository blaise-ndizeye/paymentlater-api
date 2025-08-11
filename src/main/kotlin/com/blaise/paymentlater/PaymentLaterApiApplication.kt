package com.blaise.paymentlater

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableScheduling
class PaymentLaterApiApplication

fun main(args: Array<String>) {
    runApplication<PaymentLaterApiApplication>(*args)
}
