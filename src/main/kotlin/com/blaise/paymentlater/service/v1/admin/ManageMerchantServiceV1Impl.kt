package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enum.UserRole
import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.dto.request.UpdateMerchantRequestDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

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

    override fun updateMerchant(
        merchantId: String,
        body: UpdateMerchantRequestDto
    ): MerchantProfileResponseDto {
        val merchant = merchantService.findById(ObjectId(merchantId))
        val roles: MutableList<UserRole>

        try {
            roles = merchant.roles.toMutableList().apply {
                addAll(if (body.roles != null) body.roles.map { UserRole.valueOf(it) } else emptyList())
            }
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role")
        }

        val merchantToUpdate = merchant.copy(
            name = body.name ?: merchant.name,
            email = body.email ?: merchant.email,
            webhookUrl = body.webhookUrl ?: merchant.webhookUrl,
            roles = roles.toSet(),
            updatedAt = Instant.now()
        )

        val updatedMerchant = merchantService.save(merchantToUpdate)

        return updatedMerchant.toMerchantProfileResponseDto()
    }

    override fun deactivateMerchant(merchantId: String): ResponseEntity<Unit> {
        val merchant = merchantService.findById(ObjectId(merchantId))

        if (!merchant.isActive)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant is already inactive")

        val updatedMerchant = merchant.copy(isActive = false, updatedAt = Instant.now())
        merchantService.save(updatedMerchant)

        return ResponseEntity.ok().build()
    }
}