package com.blaise.paymentlater.service.v1.merchant

import com.blaise.paymentlater.dto.shared.RegisterMerchantEventDto
import com.blaise.paymentlater.notification.MailService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * Event listener for merchant-related events with asynchronous processing.
 * 
 * Handles merchant lifecycle events that require external communication:
 * 
 * **Event Processing**:
 * - Merchant registration events for welcome email delivery
 * - Asynchronous processing to avoid blocking main business flows
 * - Integration with email notification service
 * 
 * **Async Benefits**:
 * - Non-blocking merchant registration process
 * - Improved user experience with faster API responses
 * - Resilient email delivery that doesn't impact core operations
 * - Configurable task executor for performance tuning
 */
@Component
class MerchantEventListener(
    private val mailService: MailService
) {

    /**
     * Handle merchant registration event by sending welcome email with API key.
     * 
     * Processes RegisterMerchantEventDto asynchronously to deliver
     * welcome email with newly generated API key to the merchant.
     * 
     * @param event Merchant registration event containing merchant details and API key
     */
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