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

import io.craigmiller160.spring.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.entity.AppRefreshToken
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TokenRefreshServiceTest {

    @Mock
    private lateinit var appRefreshTokenRepo: AppRefreshTokenRepository

    @Mock
    private lateinit var authServerClient: io.craigmiller160.spring.oauth2.client.AuthServerClient

    @InjectMocks
    private lateinit var tokenRefreshService: TokenRefreshService

    private val refreshToken = AppRefreshToken(
            1, "JWTID", "ABCDEFG"
    )
    private val tokenResponse = TokenResponse(
            accessToken = "DEFG",
            refreshToken = "ABCD",
            tokenId = "ID2"
    )
    private val token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImZpcnN0TmFtZSI6ImZpcnN0TmFtZSIsImxhc3ROYW1lIjoibGFzdE5hbWUiLCJjbGllbnRLZXkiOiJjbGllbnRLZXkiLCJjbGllbnROYW1lIjoiY2xpZW50TmFtZSIsInJvbGVzIjpbIlJPTEVfMSIsIlJPTEVfMiJdLCJleHAiOjE1OTQyMzg1NDQsImlhdCI6MTU5NDIzNzk0NCwianRpIjoiSldUSUQifQ.C5su_Tp1Q4ID72NgPTvG4DqipUyj7-Nh0j-zMZdLTSKb5WOzwVSpTMCPz0ipsJCqumP_OXwCt0oViquoP-b1khdabXmx5ESCoCWQXoeT9RgnYg6U-C4Yg6sB3yjXbScZ0zAEQcfq37kQx1GLJNvJZ5dDcp9YFODlppfxlfTqyV3QwBGCY1jCz8CyCZ3-IWvdB16i0gJBe81YrQn_TEUpCrsT6OLLFdRv9BKVtamJ97YGr1cksQup2riXsLNr41M3bGl4E1KD67Jbk8S8qohqkwSO3QGyj9OBvaYCrT2KsQ0YjO3GvkcsDFuT-Qp9WIBhE-5Pfov97l--ksYpNhc4yw"

    @Test
    fun test_refreshToken() {
        `when`(appRefreshTokenRepo.findByTokenId(refreshToken.tokenId))
                .thenReturn(refreshToken)
        `when`(authServerClient.authenticateRefreshToken(refreshToken.refreshToken))
                .thenReturn(tokenResponse)

        val result = tokenRefreshService.refreshToken(token)
        assertEquals(tokenResponse, result)

        verify(appRefreshTokenRepo, Mockito.times(1))
                .deleteById(1)
        verify(appRefreshTokenRepo, Mockito.times(1))
                .save(AppRefreshToken(0, tokenResponse.tokenId, tokenResponse.refreshToken))
    }

    @Test
    fun test_refreshToken_notFound() {
        val result = tokenRefreshService.refreshToken(token)
        assertNull(result)
    }

    @Test
    fun test_refreshToken_exception() {
        `when`(appRefreshTokenRepo.findByTokenId(refreshToken.tokenId))
                .thenReturn(refreshToken)
        `when`(authServerClient.authenticateRefreshToken(refreshToken.refreshToken))
                .thenThrow(RuntimeException("Dying"))

        val result = tokenRefreshService.refreshToken(token)
        assertNull(result)
    }

}
