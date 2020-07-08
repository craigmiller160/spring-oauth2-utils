package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.dto.TokenResponse

interface AuthServerClient {
    fun authenticateAuthCode(code: String): TokenResponse
    fun authenticateRefreshToken(refreshToken: String): TokenResponse
}
