package com.blaise.paymentlater.service.v1.admin

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
}