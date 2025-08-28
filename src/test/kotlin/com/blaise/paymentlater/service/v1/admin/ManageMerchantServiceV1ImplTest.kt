package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.repository.MerchantRepository
import com.blaise.paymentlater.util.TestFactory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl

@ExtendWith(MockKExtension::class)
class ManageMerchantServiceV1ImplTest {
    private val merchantRepository = mockk<MerchantRepository>()
    private lateinit var manageMerchantService: ManageMerchantServiceV1Impl

    @BeforeEach
    fun setUp() {
        manageMerchantService = ManageMerchantServiceV1Impl(merchantRepository)
        clearMocks(merchantRepository)
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
}