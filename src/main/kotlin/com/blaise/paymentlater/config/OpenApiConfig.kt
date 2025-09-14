package com.blaise.paymentlater.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI (Swagger) configuration for PaymentLater API documentation.
 * 
 * This configuration sets up comprehensive API documentation with interactive
 * Swagger UI, including security scheme definitions for both merchant and admin
 * authentication methods.
 * 
 * **Documentation Features**:
 * - Interactive API explorer with Swagger UI
 * - Comprehensive endpoint documentation
 * - Request/response schema definitions
 * - Authentication testing capabilities
 * - Security requirement specifications
 * 
 * **Supported Authentication**:
 * - **Bearer Token (JWT)**: For admin authentication
 *   - Format: `Authorization: Bearer {jwt-token}`
 *   - Used by admin users for management operations
 * - **API Key**: For merchant authentication
 *   - Format: `X-API-KEY: {api-key}`
 *   - Used by merchants for payment operations
 * 
 * **API Information**:
 * - Title: PaymentLater API
 * - Version: v1
 * - Description: Comprehensive payment processing API
 * 
 * **Access Points**:
 * - Swagger UI: `/swagger-ui.html`
 * - OpenAPI JSON: `/v3/api-docs`
 * - API Documentation: `/swagger-ui/index.html`
 * 
 * **Security Testing**:
 * - Test endpoints directly from Swagger UI
 * - Configure authentication tokens in UI
 * - Validate request/response formats
 * 
 * @see AdminSecurityConfig
 * @see MerchantSecurityConfig
 * @see JwtConfig
 * @see ApiKeyConfig
 */
@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "PaymentLater API",
        version = "v1",
        description = "API for PaymentLater"
    ),
    security = [SecurityRequirement(name = "Bearer"), SecurityRequirement(name = "X-API-KEY")]
)
class OpenApiConfig {

    /**
     * Configures OpenAPI specification with security schemes and requirements.
     * 
     * Creates a comprehensive OpenAPI configuration that defines both JWT Bearer
     * token and API key authentication schemes, enabling proper documentation
     * and testing of secured endpoints in Swagger UI.
     * 
     * **Security Schemes Configured**:
     * - **BearerToken**: HTTP Bearer scheme with JWT format
     *   - Type: HTTP authentication
     *   - Scheme: bearer
     *   - Format: JWT (JSON Web Token)
     *   - Usage: Admin authentication
     * 
     * - **ApiKey**: API key authentication via header
     *   - Type: API Key
     *   - Location: HTTP header
     *   - Header name: X-API-KEY
     *   - Usage: Merchant authentication
     * 
     * **Security Requirements**:
     * Both authentication methods are available globally, allowing endpoints
     * to specify which authentication type(s) they accept.
     * 
     * **Swagger UI Integration**:
     * - Provides "Authorize" button in Swagger UI
     * - Allows testing authenticated endpoints
     * - Shows security requirements for each endpoint
     * - Validates authentication headers automatically
     * 
     * @return Configured OpenAPI specification with security schemes
     */
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .components(
            Components()
                .addSecuritySchemes(
                    "BearerToken", SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(
                    "ApiKey", SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name("X-API-KEY")
                )
        )
        .addSecurityItem(
            io.swagger.v3.oas.models.security.SecurityRequirement()
                .addList("ApiKey")
                .addList("BearerToken")
        )
}