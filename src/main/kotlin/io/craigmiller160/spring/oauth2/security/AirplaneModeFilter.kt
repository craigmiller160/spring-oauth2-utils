package io.craigmiller160.spring.oauth2.security

import io.craigmiller160.spring.oauth2.config.AirplaneModeConfig
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AirplaneModeFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(AirplaneModeConfig.AUTHENTICATION,
                "", AirplaneModeConfig.AUTHENTICATION.authorities)
    }
}