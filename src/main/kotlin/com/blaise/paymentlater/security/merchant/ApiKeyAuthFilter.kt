package com.blaise.paymentlater.security.merchant

import com.blaise.paymentlater.service.v1.merchant.MerchantServiceV1
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthFilter(
    private val apiKeyConfig: ApiKeyConfig,
    private val merchantService: MerchantServiceV1
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = apiKeyConfig.extractFrom(request)

        if (!apiKey.isNullOrBlank()) {
            val merchant = merchantService.findByApiKey(apiKey)

            if (merchant != null && merchant.isActive) {
                val authorities = merchant.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val authentication = UsernamePasswordAuthenticationToken(
                    merchant, null, authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}