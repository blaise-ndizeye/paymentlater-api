package com.blaise.paymentlater.domain.extension

import com.blaise.paymentlater.dto.response.PageResponseDto
import org.springframework.data.domain.Page

fun <T> Page<T>.toPageResponseDto(): PageResponseDto<T> = PageResponseDto(
    content = this.content,
    page = this.number,
    size = this.size,
    totalElements = this.totalElements,
    totalPages = this.totalPages,
    last = this.isLast
)