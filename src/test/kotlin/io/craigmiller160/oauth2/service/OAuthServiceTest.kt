package io.craigmiller160.oauth2.service

import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.testutils.JwtUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class OAuthServiceTest {

    private val cookieName = "cookieName"

    @Mock
    private lateinit var oAuthConfig: OAuthConfig

    @Mock
    private lateinit var appRefreshTokenRepo: AppRefreshTokenRepository

    @InjectMocks
    private lateinit var oAuthService: OAuthService

    @Test
    fun test_logout() {
        Mockito.`when`(oAuthConfig.cookieName)
                .thenReturn(cookieName)

        val authentication = Mockito.mock(Authentication::class.java)
        val authUser = JwtUtils.createAuthUser()
        Mockito.`when`(authentication.principal)
                .thenReturn(authUser)
        SecurityContextHolder.getContext().authentication = authentication

        val cookie = oAuthService.logout()

        validateCookie(cookie, "", 0)

        Mockito.verify(appRefreshTokenRepo, Mockito.times(1))
                .removeByTokenId(authUser.tokenId)
    }

    @Test
    fun test_getAuthenticatedUser() {
        TODO("Finish this")
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

}
