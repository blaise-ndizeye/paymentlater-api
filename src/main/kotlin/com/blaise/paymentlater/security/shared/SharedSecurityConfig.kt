package com.blaise.paymentlater.security.shared

import com.blaise.paymentlater.security.admin.JwtAuthFilter
import com.blaise.paymentlater.security.merchant.ApiKeyAuthFilter
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
@Order(3)
class SharedSecurityConfig(
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun sharedSecurityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .securityMatcher(
                "/api/v1/payments/**",
                "/api/v1/transactions/**",
                "/api/v1/refunds/**"
            )
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize -> authorize.anyRequest().authenticated() }
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            }
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}