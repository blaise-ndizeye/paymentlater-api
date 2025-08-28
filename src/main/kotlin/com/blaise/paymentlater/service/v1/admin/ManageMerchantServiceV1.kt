package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.request.UpdateMerchantRequestDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import org.springframework.http.ResponseEntity

interface ManageMerchantServiceV1 {
    fun getAllMerchants(
        filter: MerchantFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<MerchantProfileResponseDto>

    fun getMerchantById(merchantId: String): MerchantProfileResponseDto

    fun updateMerchant(merchantId: String, body: UpdateMerchantRequestDto): MerchantProfileResponseDto

    fun deactivateMerchant(merchantId: String): ResponseEntity<Unit>
}