package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.enums.RefundStatus
import com.blaise.paymentlater.domain.extension.toRefundResponseDto
import com.blaise.paymentlater.repository.RefundRepository
import com.blaise.paymentlater.service.v1.admin.AdminAuthServiceV1
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import com.blaise.paymentlater.service.v1.payment.PaymentServiceV1
import com.blaise.paymentlater.service.v1.transaction.TransactionServiceV1
import com.blaise.paymentlater.util.TestFactory
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class RefundServiceV1ImplTest {
    private val refundRepository: RefundRepository = mockk()
    private val paymentService: PaymentServiceV1 = mockk()
    private val transactionService: TransactionServiceV1 = mockk()
    private val adminAuthService: AdminAuthServiceV1 = mockk()
    private val merchantAuthService: MerchantAuthServiceV1 = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk(relaxed = true)
    private lateinit var refundService: RefundServiceV1Impl

    @BeforeEach
    fun setup() {
        refundService = RefundServiceV1Impl(
            refundRepository,
            paymentService,
            transactionService,
            adminAuthService,
            merchantAuthService,
            eventPublisher
        )
        clearMocks(
            refundRepository,
            paymentService,
            transactionService,
            adminAuthService,
            merchantAuthService
        )
    }

    @Nested
    @DisplayName("APPROVE REFUND")
    inner class ApproveRefund {

        @Test
        fun `should approve refund`() {
            val refundId = "refundId"
            val transaction = TestFactory.transaction1()
            val paymentIntent = TestFactory.paymentIntent1()
            val refundServiceSpy = spyk(refundService)

            every { refundServiceSpy.findById(refundId) } returns TestFactory.refund1()
            every {
                transactionService.getTransactionAndAssociatedPaymentIntent(any())
            } returns Pair(transaction, paymentIntent)
            every { adminAuthService.getAuthenticatedAdmin() } returns TestFactory.admin()
            every { merchantAuthService.findById(any()) } returns TestFactory.merchant()
            every {
                refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id)
            } returns BigDecimal.ONE
            every { paymentService.save(any()) } returns paymentIntent
            every { transactionService.save(any()) } returns transaction
            every { refundRepository.save(any()) } returns TestFactory.refund1()
            every { eventPublisher.publishEvent(any()) } just Runs

            val result = refundServiceSpy.approveRefund(refundId)

            assertEquals(TestFactory.refund1().toRefundResponseDto(), result)
            verify(exactly = 1) { refundServiceSpy.findById(refundId) }
            verify(exactly = 1) { transactionService.getTransactionAndAssociatedPaymentIntent(any()) }
            verify(exactly = 1) { adminAuthService.getAuthenticatedAdmin() }
            verify(exactly = 1) { merchantAuthService.findById(any()) }
            verify(exactly = 1) { refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id) }
            verify(exactly = 1) { paymentService.save(any()) }
            verify(exactly = 1) { refundRepository.save(any()) }
        }

        @Test
        fun `should throw exception if refund is not pending`() {
            val refundId = "refundId"
            val refundServiceSpy = spyk(refundService)

            every {
                refundServiceSpy.findById(refundId)
            } returns TestFactory.refund1().copy(status = RefundStatus.APPROVED)

            assertThrows<ResponseStatusException> {
                refundServiceSpy.approveRefund(refundId)
            }

            verify(exactly = 1) { refundServiceSpy.findById(refundId) }
        }

        @Test
        fun `should throw exception if refund amount exceeds remaining payment intent balance`() {
            val refundId = "refundId"
            val transaction = TestFactory.transaction1()
            val paymentIntent = TestFactory.paymentIntent1()
            val refundServiceSpy = spyk(refundService)

            every { refundServiceSpy.findById(refundId) } returns TestFactory.refund1()
            every {
                transactionService.getTransactionAndAssociatedPaymentIntent(any())
            } returns Pair(transaction, paymentIntent)
            every { adminAuthService.getAuthenticatedAdmin() } returns TestFactory.admin()
            every { merchantAuthService.findById(any()) } returns TestFactory.merchant()
            every {
                refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id)
            } returns paymentIntent.amount

            assertThrows<ResponseStatusException> {
                refundServiceSpy.approveRefund(refundId)
            }

            verify(exactly = 1) { refundServiceSpy.findById(refundId) }
            verify(exactly = 1) { transactionService.getTransactionAndAssociatedPaymentIntent(any()) }
            verify(exactly = 1) { adminAuthService.getAuthenticatedAdmin() }
            verify(exactly = 1) { merchantAuthService.findById(any()) }
            verify(exactly = 1) { refundRepository.sumApprovedRefundsForPaymentIntent(paymentIntent.id) }
        }
    }
}