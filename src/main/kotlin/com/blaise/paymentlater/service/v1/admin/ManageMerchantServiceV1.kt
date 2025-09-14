package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.dto.request.UpdateMerchantRequestDto
import com.blaise.paymentlater.dto.response.MerchantProfileResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.shared.MerchantFilterDto
import org.springframework.http.ResponseEntity

/**
 * Admin merchant management service for PaymentLater platform administration.
 * 
 * Provides administrative control over merchant accounts and settings:
 * 
 * **Core Features**:
 * - Merchant profile management and updates
 * - Account activation and deactivation controls
 * - Merchant search and filtering capabilities
 * - Administrative oversight of merchant operations
 * 
 * **Administrative Functions**:
 * - View all merchant profiles with filtering
 * - Update merchant information and settings
 * - Control merchant account status (active/inactive)
 * - Monitor merchant activity and compliance
 * 
 * **Security**: All operations require admin-level authentication
 * and should be properly secured with appropriate authorization checks.
 */
interface ManageMerchantServiceV1 {
    
    /**
     * Get all merchants with filtering and pagination.
     * 
     * Supports filtering by various criteria for administrative oversight.
     * 
     * @param filter Merchant filtering criteria
     * @param page Page number (0-based)
     * @param size Page size
     * @return Paginated merchant profiles
     */
    fun getAllMerchants(
        filter: MerchantFilterDto,
        page: Int,
        size: Int
    ): PageResponseDto<MerchantProfileResponseDto>

    /**
     * Get merchant profile by ID.
     * 
     * @param merchantId Merchant identifier
     * @return Merchant profile details
     * @throws ResponseStatusException(404) if merchant not found
     */
    fun getMerchantById(merchantId: String): MerchantProfileResponseDto

    /**
     * Update merchant information.
     * 
     * Allows admins to modify merchant profile details.
     * 
     * @param merchantId Merchant to update
     * @param body Updated merchant information
     * @return Updated merchant profile
     */
    fun updateMerchant(merchantId: String, body: UpdateMerchantRequestDto): MerchantProfileResponseDto

    /**
     * Activate a merchant account.
     * 
     * Enables merchant to process payments and access API.
     */
    fun activateMerchant(merchantId: String): ResponseEntity<Unit>

    /**
     * Deactivate merchant account.
     * 
     * Disables merchant payment processing and API access.
     */
    fun deactivateMerchant(merchantId: String): ResponseEntity<Unit>
}