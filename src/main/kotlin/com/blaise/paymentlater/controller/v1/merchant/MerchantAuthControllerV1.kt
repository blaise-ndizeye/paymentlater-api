package com.blaise.paymentlater.controller.v1.merchant

import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import com.blaise.paymentlater.dto.request.RegenerateApiKeyRequestDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.MerchantResponseDto
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/merchant/auth")
class MerchantAuthControllerV1(
    private val merchantAuthService: MerchantAuthServiceV1
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody merchant: MerchantRegisterRequestDto
    ): MerchantResponseDto = merchantAuthService.register(merchant)

    @PostMapping("/regenerate-api-key")
    fun regenerateApiKey(@Valid @RequestBody body: RegenerateApiKeyRequestDto): ResponseEntity<Unit> =
        merchantAuthService.regenerateApiKey(body.email)

    @GetMapping("/me")
    @PreAuthorize("hasRole('MERCHANT')")
    fun me(@AuthenticationPrincipal merchant: Merchant): MerchantProfileResponseDto =
        merchant.toMerchantProfileResponseDto()
}