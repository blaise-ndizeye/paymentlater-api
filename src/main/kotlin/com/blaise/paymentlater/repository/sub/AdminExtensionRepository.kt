package com.blaise.paymentlater.repository.sub

import com.blaise.paymentlater.dto.response.SystemHealthResponseDto

interface AdminExtensionRepository {
    fun getSystemHealthOverview(windowHours: Long): SystemHealthResponseDto
}