package com.blaise.paymentlater.controller.v1.merchant

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Merchant Payments", description = "Merchant payments endpoints")
class MerchantPaymentControllerV1 {

}