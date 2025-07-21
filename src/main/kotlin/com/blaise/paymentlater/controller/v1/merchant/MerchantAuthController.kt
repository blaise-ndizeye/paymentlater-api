package com.blaise.paymentlater.controller.v1.merchant

import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.response.MerchantResponseDto
import com.blaise.paymentlater.service.MerchantService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/merchant/auth")
class MerchantAuthController(
    private val merchantService: MerchantService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody merchant: MerchantRegisterRequestDto
    ): MerchantResponseDto = merchantService.register(merchant)

    @PostMapping("/regenerate-api-key")
    fun regenerateApiKey(): Unit = TODO()
}