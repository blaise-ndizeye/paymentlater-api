package com.blaise.paymentlater.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

/**
 * Global security configuration for the PaymentLater API application.
 * 
 * This configuration enables method-level security annotations throughout the application,
 * allowing fine-grained authorization control using Spring Security annotations.
 * 
 * **Enabled Features**:
 * - `@PreAuthorize` and `@PostAuthorize` annotations
 * - Method-level security expressions
 * - Role-based and permission-based authorization
 * - SpEL (Spring Expression Language) support in security annotations
 * 
 * **Common Usage Examples**:
 *
 *```kotlin
 * @PreAuthorize("hasRole('ADMIN')")
 * fun adminOnlyMethod() { ... }
 *
 * @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
 * fun userOrAdminMethod(userId: String) { ... }
 * 
 * @PostAuthorize("returnObject.merchantId == authentication.name")
 * fun getMerchantData(): MerchantData { ... }
 * ```
 * 
 * **Security Annotations Supported**:
 * - `@PreAuthorize`: Check permissions before method execution
 * - `@PostAuthorize`: Check permissions after method execution
 * - `@Secured`: Simple role-based authorization
 * - `@RolesAllowed`: JSR-250 role-based authorization
 * 
 * This configuration works in conjunction with endpoint-specific security
 * configurations (AdminSecurityConfig, MerchantSecurityConfig, SharedSecurityConfig)
 * to provide both endpoint-level and method-level authorization.
 * 
 * @see AdminSecurityConfig
 * @see MerchantSecurityConfig
 * @see SharedSecurityConfig
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class GlobalSecurityConfig
