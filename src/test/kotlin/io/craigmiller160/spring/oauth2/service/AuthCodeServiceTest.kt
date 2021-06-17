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

package io.craigmiller160.spring.oauth2.service

import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.dto.TokenResponseDto
import io.craigmiller160.spring.oauth2.config.OAuthConfigImpl
import io.craigmiller160.spring.oauth2.entity.AppRefreshToken
import io.craigmiller160.spring.oauth2.exception.BadAuthCodeRequestException
import io.craigmiller160.spring.oauth2.exception.BadAuthCodeStateException
import io.craigmiller160.spring.oauth2.repository.AppRefreshTokenRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseCookie
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@ExtendWith(MockitoExtension::class)
class AuthCodeServiceTest {

    private val host = "host"
    private val path = "/path"
    private val redirectUri = "redirectUri"
    private val clientKey = "clientKey"
    private val cookieExpSecs = 30L
    private val cookieName = "cookie"
    private val postAuthRedirect = "postAuthRedirect"
    private val origin = "TheOrigin"

    @Mock
    private lateinit var oAuthConfig: OAuthConfigImpl

    @Mock
    private lateinit var authServerClient: AuthServerClient

    @Mock
    private lateinit var appRefreshTokenRepo: AppRefreshTokenRepository

    @Mock
    private lateinit var req: HttpServletRequest

    @Mock
    private lateinit var session: HttpSession

    @InjectMocks
    private lateinit var authCodeService: AuthCodeService

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_prepareAuthCodeLogin() {
        `when`(req.session)
                .thenReturn(session)
        `when`(req.getHeader("Origin"))
                .thenReturn(origin)
        `when`(oAuthConfig.authCodeLoginPath)
                .thenReturn(path)
        `when`(oAuthConfig.authCodeRedirectUri)
                .thenReturn(redirectUri)
        `when`(oAuthConfig.clientKey)
                .thenReturn(clientKey)
        `when`(oAuthConfig.authLoginBaseUri)
                .thenReturn("")

        val result = authCodeService.prepareAuthCodeLogin(req)

        val captor = ArgumentCaptor.forClass(String::class.java)

        verify(session, times(1))
                .setAttribute(eq(AuthCodeService.STATE_ATTR), captor.capture())
        verify(session, times(1))
                .setAttribute(eq(AuthCodeService.STATE_EXP_ATTR), isA())
        verify(session, times(1))
                .setAttribute(AuthCodeService.ORIGIN, origin)

        assertNotNull(captor.value)
        val state = captor.value

        val expected = "$origin$path?response_type=code&client_id=$clientKey&redirect_uri=$origin$redirectUri&state=$state"
        assertEquals(expected, result)
    }

    @Test
    fun test_code() {
        `when`(req.session)
                .thenReturn(session)
        `when`(oAuthConfig.cookieMaxAgeSecs)
                .thenReturn(cookieExpSecs)
        `when`(oAuthConfig.cookieName)
                .thenReturn(cookieName)
        `when`(oAuthConfig.postAuthRedirect)
                .thenReturn(postAuthRedirect)
        `when`(oAuthConfig.getOrDefaultCookiePath())
            .thenReturn(path)

        val authCode = "DEF"
        val state = "ABC"
        `when`(session.getAttribute(AuthCodeService.STATE_ATTR))
                .thenReturn(state)
        `when`(session.getAttribute(AuthCodeService.STATE_EXP_ATTR))
                .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1))
        `when`(session.getAttribute(AuthCodeService.ORIGIN))
                .thenReturn(origin)

        val response = TokenResponseDto("access", "refresh", "id")
        `when`(authServerClient.authenticateAuthCode(origin, authCode))
                .thenReturn(response)

        val (cookie, redirect) = authCodeService.code(req, authCode, state)
        assertEquals(postAuthRedirect, redirect)
        validateCookie(cookie, response.accessToken, cookieExpSecs)

        val manageRefreshToken = AppRefreshToken(0, response.tokenId, response.refreshToken)
        verify(appRefreshTokenRepo, Mockito.times(1))
                .save(manageRefreshToken)

        verify(session, times(1))
                .removeAttribute(AuthCodeService.STATE_ATTR)
        verify(session, times(1))
                .removeAttribute(AuthCodeService.STATE_EXP_ATTR)
        verify(session, times(1))
                .removeAttribute(AuthCodeService.ORIGIN)
        verify(appRefreshTokenRepo, Mockito.times(1))
                .removeByTokenId(response.tokenId)
    }

    private fun validateCookie(cookie: ResponseCookie, token: String, exp: Long) {
        assertEquals(cookieName, cookie.name)
        assertEquals(path, cookie.path)
        assertTrue(cookie.isSecure)
        assertTrue(cookie.isHttpOnly)
        assertEquals("strict", cookie.sameSite)
        assertEquals(token, cookie.value)
        assertEquals(Duration.ofSeconds(exp), cookie.maxAge)
    }

    @Test
    fun test_code_badState() {
        `when`(req.session)
                .thenReturn(session)
        val authCode = "DEF"
        val state = "ABC"

        val ex = assertThrows<BadAuthCodeStateException> { authCodeService.code(req, authCode, state) }
        assertEquals("State does not match expected value", ex.message)
    }

    @Test
    fun test_code_stateExp() {
        `when`(req.session)
                .thenReturn(session)
        val authCode = "DEF"
        val state = "ABC"

        `when`(session.getAttribute(AuthCodeService.STATE_ATTR))
                .thenReturn(state)
        `when`(session.getAttribute(AuthCodeService.STATE_EXP_ATTR))
                .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1))

        val ex = assertThrows<BadAuthCodeStateException> { authCodeService.code(req, authCode, state) }
        assertEquals("Auth code state has expired", ex.message)
    }

    @Test
    fun test_code_noOrigin() {
        `when`(req.session)
                .thenReturn(session)
        val authCode = "DEF"
        val state = "ABC"

        `when`(session.getAttribute(AuthCodeService.STATE_ATTR))
                .thenReturn(state)
        `when`(session.getAttribute(AuthCodeService.STATE_EXP_ATTR))
                .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1))

        val ex = assertThrows<BadAuthCodeRequestException> { authCodeService.code(req, authCode, state) }
        assertEquals("Missing origin attribute in session", ex.message)
    }

}
