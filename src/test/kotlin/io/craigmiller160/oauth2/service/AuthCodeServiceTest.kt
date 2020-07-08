package io.craigmiller160.oauth2.service

import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.entity.AppRefreshToken
import io.craigmiller160.oauth2.exception.BadAuthCodeStateException
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.testutils.JwtUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration
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

    @Mock
    private lateinit var oAuthConfig: OAuthConfig

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
        Mockito.`when`(req.session)
                .thenReturn(session)
        Mockito.`when`(oAuthConfig.authServerHost)
                .thenReturn(host)
        Mockito.`when`(oAuthConfig.authCodeLoginPath)
                .thenReturn(path)
        Mockito.`when`(oAuthConfig.authCodeRedirectUri)
                .thenReturn(redirectUri)
        Mockito.`when`(oAuthConfig.clientKey)
                .thenReturn(clientKey)

        val result = authCodeService.prepareAuthCodeLogin(req)

        val captor = ArgumentCaptor.forClass(String::class.java)

        Mockito.verify(session, Mockito.times(1))
                .setAttribute(eq(AuthCodeService.STATE_ATTR), captor.capture())

        Assertions.assertNotNull(captor.value)
        val state = captor.value

        val expected = "$host$path?response_type=code&client_id=$clientKey&redirect_uri=$redirectUri&state=$state"
        Assertions.assertEquals(expected, result)
    }

    @Test
    fun test_code() {
        Mockito.`when`(req.session)
                .thenReturn(session)
        Mockito.`when`(oAuthConfig.cookieMaxAgeSecs)
                .thenReturn(cookieExpSecs)
        Mockito.`when`(oAuthConfig.cookieName)
                .thenReturn(cookieName)
        Mockito.`when`(oAuthConfig.postAuthRedirect)
                .thenReturn(postAuthRedirect)

        val authCode = "DEF"
        val state = "ABC"
        Mockito.`when`(session.getAttribute(AuthCodeService.STATE_ATTR))
                .thenReturn(state)

        val response = TokenResponse("access", "refresh", "id")
        Mockito.`when`(authServerClient.authenticateAuthCode(authCode))
                .thenReturn(response)

        val (cookie, redirect) = authCodeService.code(req, authCode, state)
        Assertions.assertEquals(postAuthRedirect, redirect)
        validateCookie(cookie, response.accessToken, cookieExpSecs)

        val manageRefreshToken = AppRefreshToken(0, response.tokenId, response.refreshToken)
        Mockito.verify(appRefreshTokenRepo, Mockito.times(1))
                .save(manageRefreshToken)

        Mockito.verify(session, Mockito.times(1))
                .removeAttribute(AuthCodeService.STATE_ATTR)
        Mockito.verify(appRefreshTokenRepo, Mockito.times(1))
                .removeByTokenId(response.tokenId)
    }

    private fun validateCookie(cookie: ResponseCookie, token: String, exp: Long) {
        Assertions.assertEquals(cookieName, cookie.name)
        Assertions.assertEquals("/", cookie.path)
        Assertions.assertTrue(cookie.isSecure)
        Assertions.assertTrue(cookie.isHttpOnly)
        Assertions.assertEquals("strict", cookie.sameSite)
        Assertions.assertEquals(token, cookie.value)
        Assertions.assertEquals(Duration.ofSeconds(exp), cookie.maxAge)
    }

    @Test
    fun test_code_badState() {
        Mockito.`when`(req.session)
                .thenReturn(session)
        val authCode = "DEF"
        val state = "ABC"

        val ex = assertThrows<BadAuthCodeStateException> { authCodeService.code(req, authCode, state) }
        Assertions.assertEquals("State does not match expected value", ex.message)
    }

    @Test
    fun test_logout() {
        Mockito.`when`(oAuthConfig.cookieName)
                .thenReturn(cookieName)

        val authentication = Mockito.mock(Authentication::class.java)
        val authUser = JwtUtils.createAuthUser()
        Mockito.`when`(authentication.principal)
                .thenReturn(authUser)
        SecurityContextHolder.getContext().authentication = authentication

        val cookie = authCodeService.logout()

        validateCookie(cookie, "", 0)

        Mockito.verify(appRefreshTokenRepo, Mockito.times(1))
                .removeByTokenId(authUser.tokenId)
    }

}
