package com.blaise.paymentlater.service.v1.transaction

import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.dto.request.RefundTransactionRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.RefundTransactionResponseDto
import com.blaise.paymentlater.dto.response.TransactionResponseDto
import com.blaise.paymentlater.dto.shared.TransactionFilterDto
import org.bson.types.ObjectId

/**
 * Transaction management service for payment processing records.
 * 
 * Manages transaction lifecycle and refund request processing:
 * 
 * **Core Responsibilities**:
 * - Transaction CRUD operations with authorization
 * - Refund request creation and validation
 * - Transaction history and audit trails
 * - Multi-currency transaction support
 * - Role-based access control for transaction viewing
 * 
 * **Transaction Types**:
 * - Primary transactions (from payment confirmations)
 * - Refund transactions (reverse transactions)
 * - Failed transaction records
 * 
 * **Business Rules**:
 * - Transactions are immutable once created
 * - Refund requests create pending refund records
 * - Only successful transactions can be refunded
 * - Merchants can only view their own transactions
 * - All refund requests require admin approval
 */
interface TransactionServiceV1 {
    
    /** Save transaction to repository */
    fun save(transaction: Transaction): Transaction

    /**
     * Create refund request for successful transaction.
     * 
     * Validates transaction eligibility and creates pending
     * refund record awaiting admin approval.
     * 
     * @param transactionId ID of transaction to refund
     * @param body Refund request details with amount and reason
     * @return Created refund request information
     * @throws ResponseStatusException if transaction cannot be refunded
     */
    fun requestRefundTransaction(transactionId: String, body: RefundTransactionRequestDto): RefundTransactionResponseDto

    /** Find transaction by ID */
    fun findById(id: ObjectId): Transaction

    /**
     * Get transaction with its associated payment intent.
     * 
     * Convenience method for operations requiring both entities.
     * 
     * @param transactionId Transaction identifier
     * @return Pair of (Transaction, PaymentIntent)
     */
    fun getTransactionAndAssociatedPaymentIntent(transactionId: String): Pair<Transaction, PaymentIntent>

    /**
     * Get paginated transactions with filtering.
     * 
     * @param filter Transaction filtering criteria
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated transaction results
     */
    fun getTransactions(filter: TransactionFilterDto, page: Int, size: Int): PageResponseDto<TransactionResponseDto>

    /**
     * Get transaction with role-based access control.
     * 
     * @param transactionId Transaction identifier
     * @param user Authenticated user (Merchant or Admin)
     * @return Transaction details
     * @throws ResponseStatusException(403) if unauthorized access
     */
    fun getTransaction(transactionId: String, user: Any): TransactionResponseDto
}