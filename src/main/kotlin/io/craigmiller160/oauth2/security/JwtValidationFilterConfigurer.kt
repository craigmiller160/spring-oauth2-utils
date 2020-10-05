/*
 * oauth2-utils
 * Copyright (C) 2020 Craig Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
