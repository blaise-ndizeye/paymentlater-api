package com.blaise.paymentlater.service.v1.refund

import com.blaise.paymentlater.domain.model.Refund
import com.blaise.paymentlater.dto.request.RejectRefundRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.shared.RefundFilterDto

/**
 * Refund processing service for transaction reversals and merchant settlements.
 * 
 * Handles the complete refund lifecycle with comprehensive business logic:
 * 
 * **Refund Process Flow**:
 * 1. Refund request created (via separate endpoint/transaction service)
 * 2. Admin reviews and approves/rejects refund
 * 3. Approved refunds create reverse transactions
 * 4. Payment intent status updated (PARTIALLY_REFUNDED/REFUNDED)
 * 5. Email notifications sent to merchants
 * 
 * **Key Features**:
 * - Multi-currency refund support with financial integrity
 * - Partial and full refund processing
 * - Admin approval workflow with audit trails
 * - Automatic payment intent status management
 * - Event-driven email notifications
 * - Role-based access control (admin approval, merchant viewing)
 * 
 * **Business Rules**:
 * - Only PENDING refunds can be approved/rejected
 * - Total refunds cannot exceed original payment amount
 * - Approved refunds automatically create reverse transactions
 * - All refund actions are audited with admin information
 * - Merchants can only view their own refunds
 */
interface RefundServiceV1 {

    /**
     * Approve pending refund and create reverse transaction.
     * 
     * Validates refund amount against payment intent total,
     * creates reverse transaction, updates payment status,
     * and sends merchant notification.
     * 
     * @param refundId ID of pending refund to approve
     * @return Updated refund information
     * @throws ResponseStatusException(400) if refund not pending or amount exceeds limit
     */
    fun approveRefund(refundId: String): RefundTransactionResponseDto

    /**
     * Reject pending refund with explanation.
     * 
     * Updates refund status to REJECTED, records admin decision,
     * and sends notification email to merchant.
     * 
     * @param refundId ID of pending refund to reject
     * @param body Rejection details with reason
     * @return Updated refund information
     * @throws ResponseStatusException(400) if refund not pending
     */
    fun rejectRefund(refundId: String, body: RejectRefundRequestDto): RefundTransactionResponseDto

    /**
     * Get refund details with role-based access control.
     * 
     * Merchants can only view their own refunds.
     * Admins can view all refunds.
     * 
     * @param refundId Refund identifier
     * @param user Authenticated user (Merchant or Admin)
     * @return Refund details
     * @throws ResponseStatusException(403) if merchant accessing another merchant's refund
     */
    fun getRefund(refundId: String, user: Any): RefundTransactionResponseDto

    /**
     * Get paginated refunds with filtering.
     * 
     * Supports filtering by date range, merchant, status, and currency.
     * 
     * @param filter Refund filtering criteria
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated refund results
     */
    fun getRefunds(
        filter: RefundFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<RefundTransactionResponseDto>

    /**
     * Find refund by ID or throw 404 exception.
     * 
     * @param refundId Refund identifier
     * @return Refund entity
     * @throws ResponseStatusException(404) if refund not found
     */
    fun findById(refundId: String): Refund
}