package io.craigmiller160.oauth2.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseCookie
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest
class OAuthControllerTest {

    private val authCodeLoginUrl = "authCodeLoginUrl"

    @MockBean
    private lateinit var authCodeService: AuthCodeService

    @MockBean
    private lateinit var oAuthService: OAuthService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var OAuthController: OAuthController

    @Test
    fun test_login() {
        Mockito.`when`(authCodeService.prepareAuthCodeLogin(isA()))
                .thenReturn(authCodeLoginUrl)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/oauth/authcode/login")
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
                .andExpect(MockMvcResultMatchers.header().string("Location", authCodeLoginUrl))
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

        mockMvc.perform(
                MockMvcRequestBuilders.get("/oauth/authcode/code?code=$code&state=$state")
                        .secure(true)
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
                .andExpect(MockMvcResultMatchers.header().string("Location", postAuthRedirect))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", cookie.toString()))
    }

    @Test
    fun test_logout() {
        val cookie = ResponseCookie
                .from("name", "value")
                .build()
        `when`(oAuthService.logout())
                .thenReturn(cookie)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/oauth/logout")
                        .secure(true)
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", cookie.toString()))
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

        mockMvc.perform(
                MockMvcRequestBuilders.get("/oauth/user")
                        .secure(true)
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo { result ->
                    val payload = objectMapper.readValue(result.response.contentAsString, AuthUserDto::class.java)
                    assertEquals(authUser, payload)
                }
    }

}
