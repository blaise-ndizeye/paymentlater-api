package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.dto.shared.RegisterMerchantEventDto
import com.blaise.paymentlater.notification.MailService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MerchantEventListener(
    private val mailService: MailService
) {

    @Async("taskExecutor")
    @EventListener
    fun sendMerchantRegistrationEvent(event: RegisterMerchantEventDto) {
        mailService.sendMerchantRegisterApiKeyEmail(
            event.merchant.email,
            event.merchant.name,
            event.apiKey
        )
    }
}