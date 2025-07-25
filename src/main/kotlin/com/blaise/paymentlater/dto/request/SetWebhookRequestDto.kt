package com.blaise.paymentlater.dto.request

import org.hibernate.validator.constraints.URL

data class SetWebhookRequestDto(
    @field:URL(message = "Webhook URL is required")
    val webhookUrl: String
)
