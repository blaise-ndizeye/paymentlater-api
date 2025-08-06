package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.repository.PaymentIntentRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1Impl
import com.blaise.paymentlater.util.TestFactory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class PaymentServiceV1ImplTest {
    private val paymentIntentRepository: PaymentIntentRepository = mockk()
    private val merchantAuthService: MerchantAuthServiceV1Impl = mockk()
    private lateinit var paymentService: PaymentServiceV1Impl

    @BeforeEach
    fun setup() {
        paymentService = PaymentServiceV1Impl(paymentIntentRepository, merchantAuthService)
        clearMocks(paymentIntentRepository)
    }

    @Nested
    @DisplayName("GET PAYMENTS")
    inner class GetPayments {

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

            val result = paymentService.getPayments(statuses, currencies, start, end, page, size)

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

            val result = paymentService.getPayments(statuses, currencies, start, end, page, size)

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

    @Nested
    @DisplayName("CREATE PAYMENT INTENT")
    inner class CreatePaymentIntent {

        @Test
        fun `should create payment intent`() {
            val body = TestFactory.paymentIntentRequestDto()
            val merchant = TestFactory.merchant()
            val totalAmount = body.items.sumOf { it.unitAmount * it.quantity.toBigDecimal() }

            every { merchantAuthService.getAuthenticatedMerchant() } returns merchant
            every { paymentIntentRepository.save(any()) } returns TestFactory.paymentIntent1()

            val result = paymentService.createPaymentIntent(body)

            assertEquals(merchant.id.toString(), result.merchantId)
            assertEquals(body.items, result.items)
            assertEquals(totalAmount, result.amount)
            assertEquals(Currency.valueOf(body.currency), result.currency)
            assertEquals(body.metadata, result.metadata)

            verify(exactly = 1) { merchantAuthService.getAuthenticatedMerchant() }
            verify(exactly = 1) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if merchant not found`() {
            val body = TestFactory.paymentIntentRequestDto()

            every {
                merchantAuthService.getAuthenticatedMerchant()
            } throws ResponseStatusException(HttpStatus.UNAUTHORIZED)

            assertThrows<ResponseStatusException> { paymentService.createPaymentIntent(body) }
            verify(exactly = 1) { merchantAuthService.getAuthenticatedMerchant() }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }
    }
}