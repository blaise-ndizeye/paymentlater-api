package com.blaise.paymentlater.security.admin

import com.blaise.paymentlater.service.v1.admin.AdminServiceV1
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.ResponseStatusException

private val log = KotlinLogging.logger {}

@Component
class JwtAuthFilter(
    private val jwtConfig: JwtConfig,
    private val adminService: AdminServiceV1
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = jwtConfig.extractFrom(request)

        if (!authHeader.isNullOrBlank() && jwtConfig.validateAccessToken(authHeader)) {
            val username = jwtConfig.getUsernameFromToken(authHeader)
            val admin = try {
                adminService.findByUsername(username)
            } catch (_: ResponseStatusException) {
                log.warn { "Attempted to access invalid admin: $username" }
                null
            }

            if (admin != null) {
                val authorities = admin.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val authentication = UsernamePasswordAuthenticationToken(admin, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}