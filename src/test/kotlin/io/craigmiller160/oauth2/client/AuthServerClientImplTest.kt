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

package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.exception.InvalidResponseBodyException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isA
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class AuthServerClientImplTest {

    private val host = "host"
    private val path = "path"
    private val key = "key"
    private val secret = "secret"
    private val redirectUri = "redirectUri"
    private val authHeader = "Basic a2V5OnNlY3JldA=="
    private val response = TokenResponse("access", "refresh", "id")

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var oAuthConfig: OAuthConfig

    @InjectMocks
    private lateinit var authServerClient: AuthServerClientImpl

    @BeforeEach
    fun setup() {
        `when`(oAuthConfig.authServerHost)
                .thenReturn(host)
        `when`(oAuthConfig.tokenPath)
                .thenReturn(path)
        `when`(oAuthConfig.clientKey)
                .thenReturn(key)
        `when`(oAuthConfig.clientSecret)
                .thenReturn(secret)
    }

    @Test
    fun test_authenticateAuthCode() {
        `when`(oAuthConfig.authCodeRedirectUri)
                .thenReturn(redirectUri)

        val authCode = "DERFG"
        val entityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        `when`(restTemplate.exchange(
                eq("$host$path"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(TokenResponse::class.java)
        ))
                .thenReturn(ResponseEntity.ok(response))

        val result = authServerClient.authenticateAuthCode(host, authCode)
        assertEquals(response, result)

        assertEquals(1, entityCaptor.allValues.size)
        val entity = entityCaptor.value

        assertEquals(this.authHeader, entity.headers["Authorization"]?.get(0))
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED_VALUE, entity.headers["Content-Type"]?.get(0))

        val body = entity.body
        assertTrue(body is MultiValueMap<*, *>)
        val map = body as MultiValueMap<String, String>
        assertEquals("authorization_code", map["grant_type"]?.get(0))
        assertEquals("$host$redirectUri", map["redirect_uri"]?.get(0))
        assertEquals(key, map["client_id"]?.get(0))
        assertEquals(authCode, map["code"]?.get(0))
    }

    @Test
    fun test_authenticateRefreshToken_invalidResponseBody() {
        val refreshToken = "ABCDEFG"

        `when`(restTemplate.exchange(
                eq("$host$path"),
                eq(HttpMethod.POST),
                isA(HttpEntity::class.java),
                eq(TokenResponse::class.java)
        ))
                .thenReturn(ResponseEntity.noContent().build())

        assertThrows<InvalidResponseBodyException> { authServerClient.authenticateRefreshToken(refreshToken) }
    }

    @Test
    fun test_authenticateRefreshToken() {
        val refreshToken = "ABCDEFG"

        val entityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        `when`(restTemplate.exchange(
                eq("$host$path"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(TokenResponse::class.java)
        ))
                .thenReturn(ResponseEntity.ok(response))

        val result = authServerClient.authenticateRefreshToken(refreshToken)
        assertEquals(response, result)

        assertEquals(1, entityCaptor.allValues.size)
        val entity = entityCaptor.value

        assertEquals(this.authHeader, entity.headers["Authorization"]?.get(0))
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED_VALUE, entity.headers["Content-Type"]?.get(0))

        val body = entity.body
        assertTrue(body is MultiValueMap<*, *>)
        val map = body as MultiValueMap<String, String>
        assertEquals("refresh_token", map["grant_type"]?.get(0))
        assertEquals(refreshToken, map["refresh_token"]?.get(0))
    }

}
