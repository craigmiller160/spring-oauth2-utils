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

import com.nimbusds.jose.jwk.JWKSet
import io.craigmiller160.oauth2.dto.TokenResponseDto
import io.craigmiller160.spring.oauth2.config.OAuthConfigImpl
import io.craigmiller160.spring.oauth2.service.TokenRefreshService
import io.craigmiller160.spring.oauth2.testutils.JwtUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.KeyPair
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtValidationFilterTest {

    private lateinit var oAuthConfig: OAuthConfigImpl
    private lateinit var jwkSet: JWKSet
    private lateinit var jwtValidationFilter: JwtValidationFilter
    private lateinit var keyPair: KeyPair
    private lateinit var token: String
    private val cookieName = "cookie"

    @Mock
    private lateinit var tokenRefreshService: TokenRefreshService

    @Mock
    private lateinit var req: HttpServletRequest
    @Mock
    private lateinit var res: HttpServletResponse
    @Mock
    private lateinit var chain: FilterChain

    @BeforeEach
    fun setup() {
        keyPair = JwtUtils.createKeyPair()
        jwkSet = JwtUtils.createJwkSet(keyPair)
        oAuthConfig = OAuthConfigImpl().apply {
            clientKey = JwtUtils.CLIENT_KEY
            clientName = JwtUtils.CLIENT_NAME
            cookieName = cookieName
            insecurePaths = "/other/path"
        }
        oAuthConfig.jwkSet = jwkSet

        val jwt = JwtUtils.createJwt()
        token = JwtUtils.signAndSerializeJwt(jwt, keyPair.private)

        jwtValidationFilter = JwtValidationFilter(oAuthConfig, tokenRefreshService)
        `when`(req.requestURI)
                .thenReturn("/something")
    }

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_getInsecurePathPatterns() {
        val insecure = "/other/path"

        val result = jwtValidationFilter.getInsecurePathPatterns()
        assertThat(result, allOf(
                hasSize(2),
                containsInAnyOrder(
                        "/oauth/authcode/**",
                        "/other/path"
                )
        ))
    }

    @Test
    fun test_doFilterInternal_validBearerToken() {
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn("Bearer $token")

        jwtValidationFilter.doFilter(req, res, chain)
        val authentication = SecurityContextHolder.getContext().authentication
        Assertions.assertNotNull(authentication)
        val principal = authentication.principal as UserDetails
        Assertions.assertEquals(JwtUtils.USERNAME, principal.username)
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_1), authentication.authorities.toList()[0])
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_2), authentication.authorities.toList()[1])
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_defaultInsecure() {
        val mockContext = mock(SecurityContext::class.java)
        SecurityContextHolder.setContext(mockContext)

        `when`(req.requestURI)
                .thenReturn("/oauth/authcode/login")

        jwtValidationFilter.doFilter(req, res, chain)

        verify(chain, times(1))
                .doFilter(req, res)
        verify(mockContext, times(0))
                .authentication
        assertEquals(mockContext, SecurityContextHolder.getContext())
    }

    @Test
    fun test_doFilterInternal_configInsecure() {
        val mockContext = mock(SecurityContext::class.java)
        SecurityContextHolder.setContext(mockContext)

        `when`(req.requestURI)
                .thenReturn("/other/path")

        jwtValidationFilter.doFilter(req, res, chain)

        verify(chain, times(1))
                .doFilter(req, res)
        verify(mockContext, times(0))
                .authentication
        assertEquals(mockContext, SecurityContextHolder.getContext())
    }

    @Test
    fun test_doFilterInternal_noToken() {
        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_validCookie() {
        val cookie = Cookie(cookieName, token)
        Mockito.`when`(req.cookies)
                .thenReturn(arrayOf(cookie))

        jwtValidationFilter.doFilter(req, res, chain)
        val authentication = SecurityContextHolder.getContext().authentication
        Assertions.assertNotNull(authentication)
        val principal = authentication.principal as UserDetails
        Assertions.assertEquals(JwtUtils.USERNAME, principal.username)
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_1), authentication.authorities.toList()[0])
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_2), authentication.authorities.toList()[1])
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_authcodeUri() {
        Mockito.`when`(req.requestURI).thenReturn("/authcode/foo")

        jwtValidationFilter.doFilter(req, res, chain)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_badSignature() {
        val keyPair = JwtUtils.createKeyPair()
        val jwt = JwtUtils.createJwt()
        val token = JwtUtils.signAndSerializeJwt(jwt, keyPair.private)
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn("Bearer $token")

        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_wrongClient() {
        oAuthConfig.clientKey = "ABCDEFG"
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn("Bearer $token")

        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_expired() {
        val jwt = JwtUtils.createJwt(-20)
        val token = JwtUtils.signAndSerializeJwt(jwt, keyPair.private)
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn("Bearer $token")

        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_notBearer() {
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn(token)

        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_refresh() {
        val jwt = JwtUtils.createJwt(-20)
        val token = JwtUtils.signAndSerializeJwt(jwt, keyPair.private)
        Mockito.`when`(req.getHeader("Authorization"))
                .thenReturn("Bearer $token")

        val refreshToken = "ABCDEFG"
        val newRefreshToken = "HIJKLMNO"
        val newTokenId = "id2"

        Mockito.`when`(tokenRefreshService.refreshToken(token))
                .thenReturn(TokenResponseDto(this.token, newRefreshToken, newTokenId))

        jwtValidationFilter.doFilter(req, res, chain)
        val authentication = SecurityContextHolder.getContext().authentication
        Assertions.assertNotNull(authentication)
        val principal = authentication.principal as UserDetails
        Assertions.assertEquals(JwtUtils.USERNAME, principal.username)
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_1), authentication.authorities.toList()[0])
        Assertions.assertEquals(SimpleGrantedAuthority(JwtUtils.ROLE_2), authentication.authorities.toList()[1])

        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
        Mockito.verify(res, Mockito.times(1))
                .addHeader(Mockito.eq("Set-Cookie"), Mockito.anyString())
    }

}
