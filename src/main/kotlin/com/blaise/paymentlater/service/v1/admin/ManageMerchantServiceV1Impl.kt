package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class ManageMerchantServiceV1Impl(
    private val merchantRepository: MerchantRepository,
    private val merchantService: MerchantAuthServiceV1
) : ManageMerchantServiceV1 {

    override fun getAllMerchants(
        filter: MerchantFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<MerchantProfileResponseDto> {
        val merchants = merchantRepository.search(filter, page, size)

        return merchants.map { it.toMerchantProfileResponseDto() }
            .toPageResponseDto()
            .also {
                log.info { "Found ${merchants.totalElements} merchants" }
            }
    }

    override fun getMerchantById(merchantId: String): MerchantProfileResponseDto {
        val merchant = merchantService.findById(ObjectId(merchantId))
        return merchant.toMerchantProfileResponseDto()
    }
}