package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.response.AdminResponseDto
import com.blaise.paymentlater.dto.response.TokenResponseDto

/**
 * Admin authentication service for PaymentLater API management.
 * 
 * Provides secure authentication and authorization for system administrators:
 * 
 * **Core Features**:
 * - Admin registration and login with password hashing
 * - JWT access token and refresh token management
 * - Secure a token refresh mechanism with validation
 * - Spring Security integration for authenticated admin retrieval
 * - Comprehensive audit logging for security events
 * 
 * **Security Implementation**:
 * - Passwords are hashed using bcrypt before storage
 * - JWT tokens have configurable expiration times
 * - Refresh tokens are securely hashed and stored
 * - Failed login attempts are logged for security monitoring
 * - Role-based access control integration
 * 
 * **Usage**: Admins use this service to access system management features
 * including merchant management, analytics, and refund processing.
 */
interface AdminAuthServiceV1 {

    /**
     * Authenticate admin and generate JWT tokens.
     * 
     * Validates credentials and creates access/refresh token pair.
     * Failed attempts are logged for security monitoring.
     * 
     * @param body Admin login credentials
     * @return JWT access and refresh tokens
     * @throws ResponseStatusException(400) if credentials are invalid
     */
    fun login(body: AdminLoginRequestDto): TokenResponseDto

    /**
     * Register new admin with hashed password.
     * 
     * Creates new admin account with secure password hashing.
     * Prevents duplicate usernames.
     * 
     * @param body Admin registration details
     * @return Created admin information (without password)
     * @throws ResponseStatusException(400) if username already exists
     */
    fun register(body: AdminRegisterRequestDto): AdminResponseDto

    /**
     * Get currently authenticated admin from security context.
     * 
     * Retrieves admin from Spring Security authentication context.
     * Used by protected endpoints to access current admin.
     * 
     * @return Currently authenticated admin
     * @throws ResponseStatusException(401) if not authenticated or invalid principal
     */
    fun getAuthenticatedAdmin(): Admin

    /**
     * Refresh JWT tokens using valid refresh token.
     * 
     * Validates old refresh token, generates new token pair,
     * and invalidates the old refresh token for security.
     * 
     * @param oldRefreshToken Current valid refresh token
     * @return New JWT access and refresh tokens
     * @throws ResponseStatusException(400) if refresh token is invalid/expired
     */
    fun refreshToken(oldRefreshToken: String): TokenResponseDto

    /**
     * Find admin by username.
     * 
     * @param username Admin username
     * @return Admin entity
     * @throws ResponseStatusException(400) if admin not found
     */
    fun findByUsername(username: String): Admin

    /**
     * Check if admin username already exists.
     * 
     * @param username Username to check
     * @return True if username exists, false otherwise
     */
    fun existsByUsername(username: String): Boolean
}