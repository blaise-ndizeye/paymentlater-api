package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.extension.toPaymentIntentResponseDto
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import com.blaise.paymentlater.repository.PaymentIntentRepository
import com.blaise.paymentlater.repository.TransactionRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1Impl
import com.blaise.paymentlater.util.TestFactory
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class PaymentServiceV1ImplTest {
    private val paymentIntentRepository: PaymentIntentRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val merchantAuthService: MerchantAuthServiceV1Impl = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk(relaxed = true)
    private lateinit var paymentService: PaymentServiceV1Impl

    @BeforeEach
    fun setup() {
        paymentService = PaymentServiceV1Impl(
            paymentIntentRepository,
            transactionRepository,
            merchantAuthService,
            eventPublisher
        )
        clearMocks(
            paymentIntentRepository,
            transactionRepository,
            merchantAuthService,
            eventPublisher
        )
    }

    @Nested
    @DisplayName("GET PAYMENT INTENT BY ID")
    inner class GetPaymentByID {

        @Test
        fun `should get payment intent by id`() {
            val id = "123"
            val paymentIntent: PaymentIntent = TestFactory.paymentIntent1()
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent

            val result = paymentServiceSpy.getPayment(id, TestFactory.merchant())

            assertEquals(paymentIntent.toPaymentIntentResponseDto(), result)
            verify(exactly = 1) { paymentServiceSpy.findById(id) }
        }

        @Test
        fun `should throw exception if payment intent not found`() {
            val id = "123"
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.getPayment(id, TestFactory.merchant())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
        }
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

    @Nested
    @DisplayName("CANCEL PAYMENT INTENT")
    inner class CancelPaymentIntent {

        @Test
        fun `should cancel payment intent`() {
            val id = "123"
            val paymentIntent: PaymentIntent = TestFactory.paymentIntent1()
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent
            every { paymentIntentRepository.save(any()) } returns paymentIntent

            val result = paymentServiceSpy.cancelPaymentIntent(id, TestFactory.merchant())

            assertEquals(paymentIntent.toPaymentIntentResponseDto(), result)
            verify(exactly = 1) { paymentServiceSpy.findById(id) }
        }

        @Test
        fun `should throw exception if payment intent not found`() {
            val id = "123"
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.cancelPaymentIntent(id, TestFactory.merchant())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if payment status is not pending`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1().copy(status = PaymentStatus.FAILED)
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.cancelPaymentIntent(id, TestFactory.merchant())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if current date is after expiresAt`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1()
                .copy(expiresAt = Instant.now().minusSeconds(1))
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.cancelPaymentIntent(id, TestFactory.merchant())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("CONFIRM PAYMENT INTENT")
    inner class ConfirmPaymentIntent {

        @Test
        fun `should confirm payment intent`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1()
            val merchant = TestFactory.merchant()
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent
            every { merchantAuthService.getAuthenticatedMerchant() } returns merchant
            every { paymentIntentRepository.save(any()) } returns paymentIntent
            every { transactionRepository.save(any()) } returns TestFactory.transaction1()
            every { eventPublisher.publishEvent(any()) } just Runs

            val result = paymentServiceSpy.confirmPaymentIntent(
                id,
                TestFactory.confirmPaymentIntentRequestDto()
            )

            assertEquals(paymentIntent.toPaymentIntentResponseDto(), result)
            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 1) { paymentIntentRepository.save(any()) }
            verify(exactly = 1) { transactionRepository.save(any()) }
        }

        @Test
        fun `should throw exception if payment intent not found`() {
            val id = "123"
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } throws ResponseStatusException(HttpStatus.NOT_FOUND)

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.confirmPaymentIntent(id, TestFactory.confirmPaymentIntentRequestDto())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if webhook url is not set for merchant`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1()
            val merchant = TestFactory.merchant().copy(webhookUrl = null)
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent
            every { merchantAuthService.getAuthenticatedMerchant() } returns merchant

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.confirmPaymentIntent(id, TestFactory.confirmPaymentIntentRequestDto())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if payment status is not pending`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1().copy(status = PaymentStatus.FAILED)
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.confirmPaymentIntent(id, TestFactory.confirmPaymentIntentRequestDto())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }

        @Test
        fun `should throw exception if payment intent is expired`() {
            val id = "123"
            val paymentIntent = TestFactory.paymentIntent1()
                .copy(expiresAt = Instant.now().minusSeconds(1))
            val paymentServiceSpy = spyk(paymentService)

            every { paymentServiceSpy.findById(id) } returns paymentIntent
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()

            assertThrows<ResponseStatusException> {
                paymentServiceSpy.confirmPaymentIntent(id, TestFactory.confirmPaymentIntentRequestDto())
            }

            verify(exactly = 1) { paymentServiceSpy.findById(id) }
            verify(exactly = 0) { paymentIntentRepository.save(any()) }
        }
    }
}