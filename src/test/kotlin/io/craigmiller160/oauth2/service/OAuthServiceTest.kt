package io.craigmiller160.oauth2.service

import com.nhaarman.mockito_kotlin.mock
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.security.AuthenticatedUser
import io.craigmiller160.oauth2.testutils.JwtUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
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

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_logout() {
        `when`(oAuthConfig.cookieName)
                .thenReturn(cookieName)

        val authentication = Mockito.mock(Authentication::class.java)
        val authUser = JwtUtils.createAuthUser()
        `when`(authentication.principal)
                .thenReturn(authUser)
        SecurityContextHolder.getContext().authentication = authentication

        val cookie = oAuthService.logout()

        validateCookie(cookie, "", 0)

        Mockito.verify(appRefreshTokenRepo, Mockito.times(1))
                .removeByTokenId(authUser.tokenId)
    }

    @Test
    fun test_getAuthenticatedUser() {
        val authUser = AuthenticatedUser(
                userName = "User",
                grantedAuthorities = listOf(SimpleGrantedAuthority("Something")),
                firstName = "First",
                lastName = "Last",
                tokenId = "ID"
        )
        val context = mock<SecurityContext>()
        val authentication = mock<Authentication>()
        `when`(context.authentication)
                .thenReturn(authentication)
        SecurityContextHolder.setContext(context)
        `when`(authentication.principal)
                .thenReturn(authUser)

        val result = oAuthService.getAuthenticatedUser()
        assertEquals("User", result.username)
        assertEquals("First", result.firstName)
        assertEquals("Last", result.lastName)
        assertEquals(1, result.roles.size)
        assertEquals("Something", result.roles[0])
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
