package io.craigmiller160.oauth2.security

import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.service.TokenRefreshService
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component

@Component
class JwtValidationFilterConfigurer (
        private val oAuthConfig: OAuthConfig,
        private val tokenRefreshService: TokenRefreshService
) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {

    private val filter = JwtValidationFilter(oAuthConfig, tokenRefreshService)

    fun getInsecurePathPatterns(): Array<String> {
        return filter.getInsecurePathPatterns().toTypedArray()
    }

    override fun configure(http: HttpSecurity?) {
        http?.addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java)
    }

}
