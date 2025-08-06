package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
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
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
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

            every {
                paymentIntentRepository.search(any(), page, size)
            } returns PageImpl(
                listOf(
                    TestFactory.paymentIntent1(),
                    TestFactory.paymentIntent2()
                )
            )

            val result = paymentService.getPayments(
               filter = PaymentIntentFilterDto(),
               page = page,
               size = size
            )

            assertEquals(0, result.page)
            assertEquals(2, result.content.size)
            assertEquals(2, result.totalElements)

            verify(exactly = 1) {
                paymentIntentRepository.search(
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `should return empty page if no payment intents found`() {
            val page = 0
            val size = 10

            every {
                paymentIntentRepository.search(any(), page, size)
            } returns PageImpl(emptyList())

            val result = paymentService.getPayments(
                filter = PaymentIntentFilterDto(),
                page = page,
                size = size
            )

            assertEquals(0, result.page)
            assertEquals(0, result.content.size)
            assertEquals(0, result.totalElements)
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