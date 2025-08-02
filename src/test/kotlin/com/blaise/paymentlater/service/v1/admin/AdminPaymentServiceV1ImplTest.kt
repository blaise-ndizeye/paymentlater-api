package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.repository.PaymentIntentRepository
import com.blaise.paymentlater.util.TestFactory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdminPaymentServiceV1ImplTest {
    private val paymentIntentRepository: PaymentIntentRepository = mockk()
    private lateinit var paymentService: AdminPaymentServiceV1Impl

    @BeforeEach
    fun setup() {
        paymentService = AdminPaymentServiceV1Impl(paymentIntentRepository)
        clearMocks(paymentIntentRepository)
    }

    @Nested
    @DisplayName("SEARCH")
    inner class Search {

        @Test
        fun `should search payment intents`() {
            val page = 0
            val size = 10
            val statuses = listOf(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
            val currencies = listOf(Currency.RWF, Currency.USD)
            val start = Instant.parse("2022-01-01T00:00:00.000Z")
            val end = Instant.parse("2022-01-02T00:00:00.000Z")
            val pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,
                "createdAt"
            )

            every {
                paymentIntentRepository.findByAdminFilters(
                    statuses,
                    currencies = currencies.map { it.name },
                    start,
                    end,
                    pageable
                )
            } returns PageImpl(
                listOf(
                    TestFactory.paymentIntent1(),
                    TestFactory.paymentIntent2()
                )
            )

            val result = paymentService.search(statuses, currencies, start, end, page, size)

            assertEquals(0, result.page)
            assertEquals(2, result.size)
            assertEquals(1, result.totalPages)
            assertEquals(2, result.content.size)

            verify(exactly = 1) {
                paymentIntentRepository.findByAdminFilters(
                    statuses,
                    currencies = currencies.map { it.name },
                    start,
                    end,
                    pageable
                )
            }
        }

        @Test
        fun `should return empty page if no payment intents found`() {
            val page = 0
            val size = 10
            val statuses = listOf(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
            val currencies = listOf(Currency.RWF, Currency.USD)
            val start = Instant.parse("2022-01-01T00:00:00.000Z")
            val end = Instant.parse("2022-01-02T00:00:00.000Z")
            val pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.DESC,
                "createdAt"
            )

            every {
                paymentIntentRepository.findByAdminFilters(
                    statuses,
                    currencies = currencies.map { it.name },
                    start,
                    end,
                    PageRequest.of(
                        page,
                        size,
                        Sort.Direction.DESC,
                        "createdAt"
                    )
                )
            } returns PageImpl(emptyList())

            val result = paymentService.search(statuses, currencies, start, end, page, size)

            assertEquals(0, result.page)
            assertEquals(0, result.size)
            assertEquals(1, result.totalPages)
            assertEquals(0, result.content.size)

            verify(exactly = 1) {
                paymentIntentRepository.findByAdminFilters(
                    statuses,
                    currencies = currencies.map { it.name },
                    start,
                    end,
                    pageable
                )
            }
        }
    }
}