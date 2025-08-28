package com.blaise.paymentlater.dto.shared

import com.blaise.paymentlater.domain.enum.UserRole
import java.time.Instant

data class MerchantFilterDto(
    val name: String? = null,
    val email: String? = null,
    val isActive: Boolean? = null,
    val roles: List<UserRole>,
    val createdStartDate: Instant? = null,
    val createdEndDate: Instant? = null,
    val updatedStartDate: Instant? = null,
    val updatedEndDate: Instant? = null,
)
