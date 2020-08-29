package io.craigmiller160.oauth2.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import io.craigmiller160.oauth2.dto.AuthCodeLoginDto
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseCookie
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest
@AutoConfigureMockMvc
class OAuthControllerTest {

    private val authCodeLoginUrl = "authCodeLoginUrl"

    @MockBean
    private lateinit var authCodeService: AuthCodeService

    @MockBean
    private lateinit var oAuthService: OAuthService

    @Autowired
    private lateinit var provMockMvc: MockMvc

    @Autowired
    private lateinit var provObjMapper: ObjectMapper

    private lateinit var apiProcessor: ApiTestProcessor

    @BeforeEach
    fun setup() {
        apiProcessor = ApiTestProcessor {
            mockMvc = provMockMvc
            objectMapper = provObjMapper
            isSecure = true
        }
    }

    @Test
    fun test_login() {
        `when`(authCodeService.prepareAuthCodeLogin(isA()))
                .thenReturn(authCodeLoginUrl)

        val result = apiProcessor.call {
            request {
                path = "/oauth/authcode/login"
                method = HttpMethod.POST
            }
        }.convert(AuthCodeLoginDto::class.java)

        assertEquals(authCodeLoginUrl, result.url)
    }

    @Test
    fun test_code() {
        val code = "code"
        val state = "state"
        val postAuthRedirect = "postAuthRedirect"

        val cookie = ResponseCookie
                .from("name", "value")
                .build()

        `when`(authCodeService.code(isA(), eq(code), eq(state)))
                .thenReturn(Pair(cookie, postAuthRedirect))

        val result = apiProcessor.call {
            request {
                path = "/oauth/authcode/code?code=$code&state=$state"
            }
            response {
                status = 302
            }
        }

        val locationValue = result.response.getHeaderValue("Location")
        assertEquals(postAuthRedirect, locationValue)

        val cookieValue = result.response.getHeaderValue("Set-Cookie")
        assertEquals(cookie.toString(), cookieValue)
    }

    @Test
    fun test_logout() {
        val cookie = ResponseCookie
                .from("name", "value")
                .build()
        `when`(oAuthService.logout())
                .thenReturn(cookie)

        val result = apiProcessor.call {
            request {
                path = "/oauth/logout"
            }
        }

        val cookieValue = result.response.getHeaderValue("Set-Cookie")
        assertEquals(cookie.toString(), cookieValue)
    }

    @Test
    fun test_getAuthenticatedUser() {
        val authUser = AuthUserDto(
                username = "User",
                firstName = "First",
                lastName = "Last",
                roles = listOf("Something")
        )

        `when`(oAuthService.getAuthenticatedUser())
                .thenReturn(authUser)

        val result = apiProcessor.call {
            request {
                path = "/oauth/user"
            }
        }.convert(AuthUserDto::class.java)

        assertEquals(authUser, result)
    }

}
