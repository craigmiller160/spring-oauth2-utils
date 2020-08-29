package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.exception.BadAuthenticationException
import io.craigmiller160.oauth2.exception.InvalidResponseBodyException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class AuthServerClientImpl (
        private val restTemplate: RestTemplate,
        private val oAuthConfig: OAuthConfig
) : AuthServerClient {

    override fun authenticateAuthCode(origin: String, code: String): TokenResponse {
        val clientKey = oAuthConfig.clientKey
        val redirectUri = "$origin${oAuthConfig.authCodeRedirectUri}"

        val request = LinkedMultiValueMap<String,String>()
        request.add("grant_type", "authorization_code")
        request.add("client_id", clientKey)
        request.add("code", code)
        request.add("redirect_uri", redirectUri)

        return tokenRequest(request)
    }

    override fun authenticateRefreshToken(refreshToken: String): TokenResponse {
        val request = LinkedMultiValueMap<String,String>()
        request.add("grant_type", "refresh_token")
        request.add("refresh_token", refreshToken)

        return tokenRequest(request)
    }

    private fun tokenRequest(body: MultiValueMap<String, String>): TokenResponse {
        val host = oAuthConfig.authServerHost
        val path = oAuthConfig.tokenPath
        val clientKey = oAuthConfig.clientKey
        val clientSecret = oAuthConfig.clientSecret

        val url = "$host$path"

        val headers = HttpHeaders()
        headers.setBasicAuth(clientKey, clientSecret)
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        try {
            val response = restTemplate.exchange(url, HttpMethod.POST, HttpEntity<MultiValueMap<String,String>>(body, headers), TokenResponse::class.java)
            return response.body ?: throw InvalidResponseBodyException()
        } catch (ex: Exception) {
            when(ex) {
                is InvalidResponseBodyException -> throw ex
                else -> throw BadAuthenticationException("Error while requesting authentication token", ex)
            }
        }
    }

}
