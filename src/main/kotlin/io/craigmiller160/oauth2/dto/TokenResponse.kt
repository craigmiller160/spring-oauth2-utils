package io.craigmiller160.oauth2.dto

data class TokenResponse (
        val accessToken: String,
        val refreshToken: String,
        val tokenId: String
)
