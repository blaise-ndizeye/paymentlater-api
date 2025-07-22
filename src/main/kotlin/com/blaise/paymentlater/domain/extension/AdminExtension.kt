package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.response.AdminResponseDto

fun Admin.toAdminResponseDto(): AdminResponseDto = AdminResponseDto(
    id = id.toString(),
    username = username,
    createdAt = createdAt.toString()
)