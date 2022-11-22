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

package io.craigmiller160.spring.oauth2.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import io.craigmiller160.oauth2.dto.AuthCodeLoginDto
import io.craigmiller160.oauth2.dto.AuthCodeSuccessDto
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuth2Service
import io.craigmiller160.spring.oauth2.config.AirplaneModeConfig
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
    private lateinit var oAuthService: OAuth2Service
    @MockBean
    private lateinit var airplaneModeConfig: AirplaneModeConfig

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
                .toString()

        `when`(authCodeService.code(isA(), eq(code), eq(state)))
                .thenReturn(AuthCodeSuccessDto(cookie, postAuthRedirect))

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
        assertEquals(cookie, cookieValue)
    }

    @Test
    fun test_logout() {
        val cookie = ResponseCookie
                .from("name", "value")
                .build()
                .toString()
        `when`(oAuthService.logout())
                .thenReturn(cookie)

        val result = apiProcessor.call {
            request {
                path = "/oauth/logout"
            }
        }

        val cookieValue = result.response.getHeaderValue("Set-Cookie")
        assertEquals(cookie, cookieValue)
    }

    @Test
    fun test_getAuthenticatedUser_airplaneMode() {
        `when`(airplaneModeConfig.isAirplaneMode())
            .thenReturn(true)

        val result = apiProcessor.call {
            request {
                path = "/oauth/user"
            }
        }.convert(AuthUserDto::class.java)

        assertEquals(AirplaneModeConfig.AUTH_USER, result)
    }

    @Test
    fun test_getAuthenticatedUser() {
        `when`(airplaneModeConfig.isAirplaneMode())
            .thenReturn(false)
        val authUser = AuthUserDto(
                userId = 1,
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
