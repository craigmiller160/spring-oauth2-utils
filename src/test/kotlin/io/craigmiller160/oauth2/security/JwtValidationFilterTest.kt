package io.craigmiller160.oauth2.security

import com.nimbusds.jose.jwk.JWKSet
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.service.TokenRefreshService
import io.craigmiller160.oauth2.testutils.JwtUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.KeyPair
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
class JwtValidationFilterTest {

    private lateinit var oAuthConfig: OAuthConfig
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
        oAuthConfig = OAuthConfig(
                clientKey = JwtUtils.CLIENT_KEY,
                clientName = JwtUtils.CLIENT_NAME,
                cookieName = cookieName
        )
        oAuthConfig.jwkSet = jwkSet

        val jwt = JwtUtils.createJwt()
        token = JwtUtils.signAndSerializeJwt(jwt, keyPair.private)

        jwtValidationFilter = JwtValidationFilter(oAuthConfig, tokenRefreshService)
        Mockito.`when`(req.requestURI)
                .thenReturn("/something")
    }

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_getInsecurePathPatterns() {
        TODO("Finish this")
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
        TODO("Finish this")
    }

    @Test
    fun test_doFilterInternal_configInsecure() {
        TODO("Finish this")
    }

    @Test
    fun test_doFilterInternal_noToken() {
        jwtValidationFilter.doFilter(req, res, chain)
        Assertions.assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verify(chain, Mockito.times(1))
                .doFilter(req, res)
    }

    @Test
    fun test_doFilterInternal_cookie() {
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
                .thenReturn(TokenResponse(this.token, newRefreshToken, newTokenId))

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
