package io.craigmiller160.oauth2.service

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.dto.TokenResponse
import io.craigmiller160.oauth2.entity.AppRefreshToken
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class TokenRefreshService (
        private val appRefreshTokenRepo: AppRefreshTokenRepository,
        private val authServerClient: AuthServerClient
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun refreshToken(token: String): TokenResponse? {
        val jwt = SignedJWT.parse(token)
        val claims = jwt.jwtClaimsSet
        return appRefreshTokenRepo.findByTokenId(claims.jwtid)
                ?.let { refreshToken ->
                    try {
                        val tokenResponse = authServerClient.authenticateRefreshToken(refreshToken.refreshToken)
                        appRefreshTokenRepo.deleteById(refreshToken.id)
                        appRefreshTokenRepo.save(AppRefreshToken(0, tokenResponse.tokenId, tokenResponse.refreshToken))
                        tokenResponse
                    } catch (ex: Exception) {
                        log.error("Error refreshing token", ex)
                        null
                    }
                }
    }

}
