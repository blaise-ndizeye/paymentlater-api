package com.blaise.paymentlater.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class TransactionMetadataRequestDto(
    @field:Email
    val customerEmail: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$")
    val customerPhone: String? = null,

    val customerName: String? = null,

    @field:NotBlank
    val referenceId: String, // Link to merchant's internal order

    val description: String? = null,

    // These fields are optional but contextually important
    val failureReason: String? = null, // Required if status = FAILED
    val refundReason: String? = null,  // Required if status = REFUND
    val gatewayResponseCode: String? = null,

    val extra: Map<String, Any?> = emptyMap() // Flexible space for integration-specific data

)