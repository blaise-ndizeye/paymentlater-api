package com.blaise.paymentlater.security.admin

import com.blaise.paymentlater.domain.enums.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@Order(2)
class AdminSecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun adminFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .securityMatcher("/api/v1/admin/**")
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/api/v1/admin/auth/login",
                        "/api/v1/admin/auth/register",
                        "/api/v1/admin/auth/refresh-token"
                    ).permitAll()
                    .anyRequest().hasRole(UserRole.ADMIN.name)
            }
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}