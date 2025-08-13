package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.enums.TransactionStatus
import com.blaise.paymentlater.domain.extension.toRefundResponseDto
import com.blaise.paymentlater.repository.RefundRepository
import com.blaise.paymentlater.repository.TransactionRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
import com.blaise.paymentlater.util.TestFactory
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class TransactionServiceV1ImplTest {
    private val transactionRepository: TransactionRepository = mockk()
    private val refundRepository: RefundRepository = mockk()
    private val paymentService: PaymentServiceV1 = mockk()
    private val merchantAuthService: MerchantAuthServiceV1 = mockk()
    private lateinit var transactionService: TransactionServiceV1Impl

    @BeforeEach
    fun setup() {
        transactionService = TransactionServiceV1Impl(
            transactionRepository,
            refundRepository,
            paymentService,
            merchantAuthService
        )
        clearMocks(
            transactionRepository,
            refundRepository,
            paymentService,
            merchantAuthService
        )
    }

    @Nested
    @DisplayName("REQUEST REFUND TRANSACTION")
    inner class RequestRefundTransaction {

        @Test
        fun `should request refund transaction`() {
            val transactionId = "trans123"
            val body = TestFactory.refundTransactionRequestDto()
            val transactionServiceSpy = spyk(transactionService)

            every { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) } returns Pair(
                TestFactory.transaction1(),
                TestFactory.paymentIntent1().copy(status = PaymentStatus.COMPLETED)
            )
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()
            every {
                refundRepository.sumApprovedRefundsForPaymentIntent(any())
            } returns body.amount
            every { refundRepository.save(any()) } returns TestFactory.refund1()

            val result = transactionServiceSpy.requestRefundTransaction(transactionId, body)

            assertEquals(TestFactory.refund1().toRefundResponseDto(), result)
            verify(exactly = 1) { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) }
            verify(exactly = 1) { refundRepository.save(any()) }
        }

        @Test
        fun `should throw exception if merchant's id is not the same as payment intent's merchant id`() {
            val transactionId = "trans123"
            val body = TestFactory.refundTransactionRequestDto()
            val transactionServiceSpy = spyk(transactionService)

            every { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) } returns Pair(
                TestFactory.transaction1(),
                TestFactory.paymentIntent1().copy(merchantId = ObjectId("688343c2b89f9cf214b8aae7"))
            )
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()

            assertThrows<ResponseStatusException> {
                transactionServiceSpy.requestRefundTransaction(transactionId, body)
            }

            verify { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) wasNot Called }
            verify { refundRepository.save(any()) wasNot Called }
        }

        @Test
        fun `should throw exception if payment intent is not completed`() {
            val transactionId = "trans123"
            val body = TestFactory.refundTransactionRequestDto()
            val transactionServiceSpy = spyk(transactionService)

            every { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) } returns Pair(
                TestFactory.transaction1(),
                TestFactory.paymentIntent1().copy(status = PaymentStatus.PENDING)
            )
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()

            assertThrows<ResponseStatusException> {
                transactionServiceSpy.requestRefundTransaction(transactionId, body)
            }

            verify { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) wasNot Called }
            verify { refundRepository.save(any()) wasNot Called }
        }

        @Test
        fun `should throw exception if transaction status is not successful`() {
            val transactionId = "trans123"
            val body = TestFactory.refundTransactionRequestDto()
            val transactionServiceSpy = spyk(transactionService)

            every { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) } returns Pair(
                TestFactory.transaction1().copy(status = TransactionStatus.FAILED),
                TestFactory.paymentIntent1().copy(status = PaymentStatus.COMPLETED)
            )
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()

            assertThrows<ResponseStatusException> {
                transactionServiceSpy.requestRefundTransaction(transactionId, body)
            }

            verify { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) wasNot Called }
            verify { refundRepository.save(any()) wasNot Called }
        }

        @Test
        fun `should throw exception if requested refund amount exceeds remaining refundable balance`() {
            val transactionId = "trans123"
            val transaction = TestFactory.transaction1()
            val body = TestFactory.refundTransactionRequestDto().copy(amount = BigDecimal.TEN)
            val paymentIntent = TestFactory.paymentIntent1().copy(
                status = PaymentStatus.COMPLETED,
                amount = BigDecimal.TEN.times(BigDecimal.TWO)
            )
            val transactionServiceSpy = spyk(transactionService)

            every { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) } returns Pair(
                transaction,
                paymentIntent
            )
            every { merchantAuthService.getAuthenticatedMerchant() } returns TestFactory.merchant()
            every {
                refundRepository.sumApprovedRefundsForPaymentIntent(any())
            } returns paymentIntent.amount

            assertThrows<ResponseStatusException> {
                transactionServiceSpy.requestRefundTransaction(transactionId, body)
            }

            verify { transactionServiceSpy.getTransactionAndAssociatedPaymentIntent(transactionId) wasNot Called }
            verify { refundRepository.save(any()) wasNot Called }
        }
    }
}