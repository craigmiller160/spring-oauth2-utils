package io.craigmiller160.oauth2.controller

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuthService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
    private lateinit var OAuthController: OAuthController

    @Test
    fun test_login() {
        Mockito.`when`(authCodeService.prepareAuthCodeLogin(isA()))
                .thenReturn(authCodeLoginUrl)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authcode/login")
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

        Mockito.`when`(authCodeService.code(isA(), eq(code), eq(state)))
                .thenReturn(Pair(cookie, postAuthRedirect))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authcode/code?code=$code&state=$state")
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
        Mockito.`when`(oAuthService.logout())
                .thenReturn(cookie)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authcode/logout")
                        .secure(true)
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie", cookie.toString()))
    }

}
