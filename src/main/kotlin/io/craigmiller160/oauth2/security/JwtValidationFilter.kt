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

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.BadJOSEException
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.exception.InvalidTokenException
import io.craigmiller160.oauth2.service.TokenRefreshService
import io.craigmiller160.oauth2.util.CookieCreator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.text.ParseException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtValidationFilter (
        private val oAuthConfig: OAuthConfig,
        private val tokenRefreshService: TokenRefreshService
) : OncePerRequestFilter() {

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private val defaultInsecureUriPatterns = listOf("/oauth/authcode/**", "/oauth/logout")
    private val insecurePathPatterns = oAuthConfig.getInsecurePathList()

    fun getInsecurePathPatterns(): List<String> {
        return defaultInsecureUriPatterns + insecurePathPatterns
    }

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        if (isUriSecured(req.requestURI)) {
            log.debug("Authenticating access for secured URI: ${req.requestURI}")
            try {
                val token = getToken(req)
                val claims = validateToken(token, res)
                SecurityContextHolder.getContext().authentication = createAuthentication(claims)
            } catch (ex: InvalidTokenException) {
                log.error("Token Validation Failed: ${ex.message}")
                log.debug("", ex)
                SecurityContextHolder.clearContext()
            }
        } else {
            log.debug("Skipping authentication for insecure URI: ${req.requestURI}")
        }

        chain.doFilter(req, res)
    }

    private fun isUriSecured(requestUri: String): Boolean {
        val antMatcher = AntPathMatcher()
        return defaultInsecureUriPatterns.firstOrNull { antMatcher.match(it, requestUri) } == null &&
                insecurePathPatterns.firstOrNull { antMatcher.match(it, requestUri) } == null
    }

    private fun validateToken(token: String, res: HttpServletResponse, alreadyAttemptedRefresh: Boolean = false): JWTClaimsSet {
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>()
        val keySource = ImmutableJWKSet<SecurityContext>(oAuthConfig.jwkSet)
        val expectedAlg = JWSAlgorithm.RS256
        val keySelector = JWSVerificationKeySelector(expectedAlg, keySource)
        jwtProcessor.jwsKeySelector = keySelector

        val claimsVerifier = DefaultJWTClaimsVerifier<SecurityContext>(
                JWTClaimsSet.Builder()
                        .claim("clientKey", oAuthConfig.clientKey)
                        .claim("clientName", oAuthConfig.clientName)
                        .build(),
                setOf("sub", "exp", "iat", "jti")
        )
        jwtProcessor.jwtClaimsSetVerifier = claimsVerifier

        try {
            return jwtProcessor.process(token, null)
        } catch (ex: Exception) {
            when(ex) {
                is BadJOSEException -> {
                    if (alreadyAttemptedRefresh) {
                        throw InvalidTokenException("Token validation failed: ${ex.message}", ex)
                    }

                    try {
                        return tokenRefreshService.refreshToken(token)
                                ?.let { tokenResponse ->
                                    val claims = validateToken(tokenResponse.accessToken, res, true)
                                    res.addHeader("Set-Cookie", CookieCreator.create(oAuthConfig.cookieName, tokenResponse.accessToken, oAuthConfig.cookieMaxAgeSecs).toString())
                                    claims
                                }
                                ?: throw InvalidTokenException("Token validation failed: ${ex.message}", ex)
                    } catch (ex: Exception) {
                        when (ex) {
                            is InvalidTokenException -> throw ex
                            else -> throw InvalidTokenException("Token refresh error", ex)
                        }
                    }
                }
                is ParseException, is JOSEException ->
                    throw InvalidTokenException("Token validation failed: ${ex.message}", ex)
                is RuntimeException -> throw ex
                else -> throw RuntimeException(ex)
            }
        }
    }

    private fun createAuthentication(claims: JWTClaimsSet): Authentication {
        val authorities = claims.getStringListClaim("roles")
                .map { SimpleGrantedAuthority(it) }
        val authUser = AuthenticatedUser(
                userName = claims.subject,
                grantedAuthorities = authorities,
                firstName = claims.getStringClaim("firstName"),
                lastName = claims.getStringClaim("lastName"),
                tokenId = claims.jwtid
        )
        return UsernamePasswordAuthenticationToken(authUser, "", authUser.authorities)
    }

    private fun getToken(req: HttpServletRequest): String {
        return getBearerToken(req)
                ?: getCookieToken(req)
                ?: throw InvalidTokenException("Token not found")
    }

    private fun getCookieToken(req: HttpServletRequest): String? {
        return req.cookies?.find { cookie -> cookie.name == oAuthConfig.cookieName }?.value
    }

    private fun getBearerToken(req: HttpServletRequest): String? {
        val token = req.getHeader("Authorization")
        return token?.let {
            if (!it.startsWith("Bearer ")) {
                throw InvalidTokenException("Not bearer token")
            }
            it.replace("Bearer ", "")
        }

    }


}
