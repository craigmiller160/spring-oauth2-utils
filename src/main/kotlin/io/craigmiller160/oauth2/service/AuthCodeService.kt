package io.craigmiller160.oauth2.service

import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.entity.AppRefreshToken
import io.craigmiller160.oauth2.exception.BadAuthCodeStateException
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.security.AuthenticatedUser
import io.craigmiller160.oauth2.util.CookieCreator
import org.springframework.http.ResponseCookie
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.servlet.http.HttpServletRequest

@Service
class AuthCodeService (
        private val oAuthConfig: OAuthConfig,
        private val authServerClient: AuthServerClient,
        private val appRefreshTokenRepo: AppRefreshTokenRepository
) {

    companion object {
        const val STATE_ATTR = "state"
    }

    private fun generateAuthCodeState(): String {
        val random = SecureRandom()
        val bigInt = BigInteger(130, random)
        return bigInt.toString(32)
    }

    fun prepareAuthCodeLogin(req: HttpServletRequest): String {
        val state = generateAuthCodeState()
        req.session.setAttribute(STATE_ATTR, state)

        val host = oAuthConfig.authServerHost
        val loginPath = oAuthConfig.authCodeLoginPath
        val redirectUri = URLEncoder.encode(oAuthConfig.authCodeRedirectUri, StandardCharsets.UTF_8)
        val clientKey = URLEncoder.encode(oAuthConfig.clientKey, StandardCharsets.UTF_8)
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8)

        return "$host$loginPath?response_type=code&client_id=$clientKey&redirect_uri=$redirectUri&state=$encodedState"
    }

    fun code(req: HttpServletRequest, code: String, state: String): Pair<ResponseCookie,String> {
        val expectedState = req.session.getAttribute(STATE_ATTR) as String?
        if (expectedState != state) {
            throw BadAuthCodeStateException("State does not match expected value")
        }

        req.session.removeAttribute(STATE_ATTR)

        val tokens = authServerClient.authenticateAuthCode(code)
        val manageRefreshToken = AppRefreshToken(0, tokens.tokenId, tokens.refreshToken)
        appRefreshTokenRepo.removeByTokenId(tokens.tokenId)
        appRefreshTokenRepo.save(manageRefreshToken)
        val cookie = CookieCreator.create(oAuthConfig.cookieName, tokens.accessToken, oAuthConfig.cookieMaxAgeSecs)
        return Pair(cookie, oAuthConfig.postAuthRedirect)
    }

}
