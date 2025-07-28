package com.blaise.paymentlater.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.URL

@Schema(description = "Set webhook request details")
data class SetWebhookRequestDto(
    @field:URL(message = "Webhook URL is required")
    val webhookUrl: String
)
