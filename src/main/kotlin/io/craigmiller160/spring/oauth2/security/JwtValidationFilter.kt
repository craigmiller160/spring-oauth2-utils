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

package io.craigmiller160.spring.oauth2.security

import com.nimbusds.jwt.JWTClaimsSet
import io.craigmiller160.oauth2.security.AuthenticationFilterService
import io.craigmiller160.oauth2.security.RequestWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtValidationFilter (
        private val authenticationFilterService: AuthenticationFilterService
) : OncePerRequestFilter() {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        authenticationFilterService.authenticateRequest(RequestWrapper(
                requestUri = req.requestURI,
                getCookieValue = { cookieName ->
                    req.cookies?.find { cookie -> cookie.name == cookieName }?.value
                },
                getHeaderValue = { headerName -> req.getHeader(headerName) },
                setAuthentication = { claims ->
                    SecurityContextHolder.getContext().authentication = createAuthentication(claims)
                },
                setNewTokenCookie = { cookie -> res.addHeader("Set-Cookie", cookie) }
        ))
                .onFailure { ex ->
                    log.error("Token Validation Failed: ${ex.message}")
                    log.debug("", ex)
                    SecurityContextHolder.clearContext()
                }
        chain.doFilter(req, res)
    }

    private fun createAuthentication(claims: JWTClaimsSet): Authentication {
        val roles = claims.getStringListClaim("roles")
        val authUser = AuthenticatedUserDetails(
                userName = claims.subject,
                roles = roles,
                firstName = claims.getStringClaim("firstName"),
                lastName = claims.getStringClaim("lastName"),
                tokenId = claims.jwtid
        )
        return UsernamePasswordAuthenticationToken(authUser, "", authUser.authorities)
    }


}
