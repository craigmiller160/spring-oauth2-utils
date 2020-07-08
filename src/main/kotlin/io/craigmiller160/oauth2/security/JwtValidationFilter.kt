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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
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

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) { // TODO refreshed token needs to be added to response as cookie
        if (!req.requestURI.startsWith("/authcode")) {
            try {
                val token = getToken(req)
                val claims = validateToken(token, res)
                SecurityContextHolder.getContext().authentication = createAuthentication(claims)
            } catch (ex: InvalidTokenException) {
                log.error("Error authenticating token", ex)
                SecurityContextHolder.clearContext()
            }
        }

        chain.doFilter(req, res)
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
                        throw InvalidTokenException("Token validation failed", ex)
                    }

                    return tokenRefreshService.refreshToken(token)
                            ?.let { tokenResponse ->
                                val claims = validateToken(tokenResponse.accessToken, res, true)
                                res.addHeader("Set-Cookie", createCookie(tokenResponse.accessToken, oAuthConfig.cookieMaxAgeSecs).toString())
                                claims
                            }
                            ?: throw InvalidTokenException("Token validation failed", ex)
                }
                is ParseException, is JOSEException ->
                    throw InvalidTokenException("Token validation failed", ex)
                is RuntimeException -> throw ex
                else -> throw RuntimeException(ex)
            }
        }
    }

    // TODO add to utility class
    private fun createCookie(token: String, maxAge: Long): ResponseCookie {
        return ResponseCookie
                .from(oAuthConfig.cookieName, token)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("strict")
                .build()
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
        var token: String? = null
        if (oAuthConfig.acceptBearerToken) {
            token = getBearerToken(req)
        }

        if (token == null && oAuthConfig.acceptCookie) {
            token = getCookieToken(req)
        }

        if (token == null) {
            throw InvalidTokenException("Token not found")
        }

        return token
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