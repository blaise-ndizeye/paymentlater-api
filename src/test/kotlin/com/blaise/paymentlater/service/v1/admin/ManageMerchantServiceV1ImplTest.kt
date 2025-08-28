package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enum.UserRole
import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1Impl
import com.blaise.paymentlater.util.TestFactory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@ExtendWith(MockKExtension::class)
class ManageMerchantServiceV1ImplTest {
    private val merchantRepository = mockk<MerchantRepository>()
    private val merchantService = mockk<MerchantAuthServiceV1Impl>()
    private lateinit var manageMerchantService: ManageMerchantServiceV1Impl

    @BeforeEach
    fun setUp() {
        manageMerchantService = ManageMerchantServiceV1Impl(merchantRepository, merchantService)
        clearMocks(merchantRepository, merchantService)
    }

    @Nested
    @DisplayName("GET ALL MERCHANTS")
    inner class GetAllMerchants {

        @Test
        fun `should return all merchants`() {
            val page = 0
            val size = 10
            val filter = TestFactory.merchantFilterDto()

            every {
                merchantRepository.search(filter, page, size)
            } returns PageImpl(
                listOf(
                    TestFactory.merchant1(),
                    TestFactory.merchant2()
                )
            )

            val result = manageMerchantService.getAllMerchants(filter, page, size)

            assertEquals(2, result.content.size)
            assertEquals(2, result.totalElements)
            verify(exactly = 1) { merchantRepository.search(filter, page, size) }
        }
    }

    @Nested
    @DisplayName("GET MERCHANT BY ID")
    inner class GetMerchantById {

        @Test
        fun `should return merchant by id`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val merchant = TestFactory.merchant1()

            every { merchantService.findById(ObjectId(merchantId)) } returns merchant

            val result = manageMerchantService.getMerchantById(merchantId)

            assertEquals(merchant.toMerchantProfileResponseDto(), result)
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }

        @Test
        fun `should throw exception when merchant not found`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"

            every {
                merchantService.findById(ObjectId(merchantId))
            } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                manageMerchantService.getMerchantById(merchantId)
            }
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }
    }

    @Nested
    @DisplayName("UPDATE MERCHANT")
    inner class UpdateMerchant {

        @Test
        fun `should update merchant`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val requestDto = TestFactory.updateMerchantRequestDto()
            val merchant = TestFactory.merchant1()
            val updatedMerchant = merchant.copy(
                name = requestDto.name!!,
                email = requestDto.email!!,
                webhookUrl = requestDto.webhookUrl,
                roles = requestDto.roles!!.map { UserRole.valueOf(it) }.toSet(),
                updatedAt = Instant.now()
            )

            every { merchantService.findById(ObjectId(merchantId)) } returns merchant
            every { merchantService.save(any()) } returns updatedMerchant

            val result = manageMerchantService.updateMerchant(merchantId, requestDto)

            assertEquals(updatedMerchant.toMerchantProfileResponseDto(), result)
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
            verify(exactly = 1) { merchantService.save(any()) }
        }

        @Test
        fun `should throw exception when merchant not found`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val requestDto = TestFactory.updateMerchantRequestDto()

            every {
                merchantService.findById(ObjectId(merchantId))
            } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                manageMerchantService.updateMerchant(merchantId, requestDto)
            }
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }

        @Test
        fun `should throw exception when roles are invalid`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val requestDto = TestFactory.updateMerchantRequestDto().copy(
                roles = listOf("INVALID_ROLE")
            )
            every { merchantService.findById(ObjectId(merchantId)) } returns TestFactory.merchant1()

            assertThrows<IllegalArgumentException> {
                manageMerchantService.updateMerchant(merchantId, requestDto)
            }
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }
    }

    @Nested
    @DisplayName("DEACTIVATE MERCHANT")
    inner class DeactivateMerchant {

        @Test
        fun `should deactivate merchant`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val merchant = TestFactory.merchant1()

            every { merchantService.findById(ObjectId(merchantId)) } returns merchant
            every { merchantService.save(any()) } returns merchant.copy(isActive = false)

            val result = manageMerchantService.deactivateMerchant(merchantId)

            assertEquals(HttpStatus.OK, result.statusCode)
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
            verify(exactly = 1) { merchantService.save(any()) }
        }

        @Test
        fun `should throw exception when merchant is already inactive`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"
            val merchant = TestFactory.merchant1()

            every { merchantService.findById(ObjectId(merchantId)) } returns merchant.copy(isActive = false)

            assertThrows<ResponseStatusException> {
                manageMerchantService.deactivateMerchant(merchantId)
            }
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }

        @Test
        fun `should throw exception when merchant not found`() {
            val merchantId = "64d1b3b3b3b3b3b3b3b3b3b3"

            every {
                merchantService.findById(ObjectId(merchantId))
            } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                manageMerchantService.deactivateMerchant(merchantId)
            }
            verify(exactly = 1) { merchantService.findById(ObjectId(merchantId)) }
        }
    }
}