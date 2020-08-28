package io.craigmiller160.oauth2.service

import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.entity.AppRefreshToken
import io.craigmiller160.oauth2.exception.BadAuthCodeRequestException
import io.craigmiller160.oauth2.exception.BadAuthCodeStateException
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.util.CookieCreator
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@Service
class AuthCodeService (
        private val oAuthConfig: OAuthConfig,
        private val authServerClient: AuthServerClient,
        private val appRefreshTokenRepo: AppRefreshTokenRepository
) {

    companion object {
        const val STATE_ATTR = "state"
        const val STATE_EXP_ATTR = "stateExp"
        const val ORIGIN = "origin"
    }

    private fun generateAuthCodeState(): String {
        val random = SecureRandom()
        val bigInt = BigInteger(130, random)
        return bigInt.toString(32)
    }

    fun prepareAuthCodeLogin(req: HttpServletRequest): String { // TODO remember docs and tests overhaul
        val origin = req.getHeader("Origin")
                ?: throw BadAuthCodeRequestException("Missing origin header on request")

        val state = generateAuthCodeState()
        req.session.setAttribute(STATE_ATTR, state)
        req.session.setAttribute(STATE_EXP_ATTR, LocalDateTime.now().plusMinutes(oAuthConfig.authCodeWaitMins))
        req.session.setAttribute(ORIGIN, origin)

        val loginPath = oAuthConfig.authCodeLoginPath
        val clientKey = URLEncoder.encode(oAuthConfig.clientKey, StandardCharsets.UTF_8)
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8)

        val redirectUri = URLEncoder.encode("$origin${oAuthConfig.authCodeRedirectUri}", StandardCharsets.UTF_8)
        val host = "$origin${oAuthConfig.authLoginBaseUri}"

        return "$host$loginPath?response_type=code&client_id=$clientKey&redirect_uri=$redirectUri&state=$encodedState"
    }

    fun code(req: HttpServletRequest, code: String, state: String): Pair<ResponseCookie,String> {
        val expectedState = req.session.getAttribute(STATE_ATTR) as String?
        val stateExp = req.session.getAttribute(STATE_EXP_ATTR) as LocalDateTime?
        if (expectedState != state) {
            throw BadAuthCodeStateException("State does not match expected value")
        }
        if (stateExp == null || LocalDateTime.now() > stateExp) {
            throw BadAuthCodeStateException("Auth code state has expired")
        }

        val origin = req.session.getAttribute(ORIGIN) as String?
                ?: throw BadAuthCodeRequestException("Missing origin attribute in session")

        req.session.removeAttribute(STATE_ATTR) // TODO add this all to tests
        req.session.removeAttribute(STATE_EXP_ATTR)
        req.session.removeAttribute(ORIGIN)

        val tokens = authServerClient.authenticateAuthCode(origin, code)
        val manageRefreshToken = AppRefreshToken(0, tokens.tokenId, tokens.refreshToken)
        appRefreshTokenRepo.removeByTokenId(tokens.tokenId)
        appRefreshTokenRepo.save(manageRefreshToken)
        val cookie = CookieCreator.create(oAuthConfig.cookieName, tokens.accessToken, oAuthConfig.cookieMaxAgeSecs)
        return Pair(cookie, oAuthConfig.postAuthRedirect)
    }

}
