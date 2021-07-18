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

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.domain.entity.AppRefreshToken
import io.craigmiller160.oauth2.domain.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.dto.TokenResponseDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

// TODO delete this
@Service
class TokenRefreshService (
        private val appRefreshTokenRepo: AppRefreshTokenRepository,
        private val authServerClient: AuthServerClient
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun refreshToken(accessToken: String): TokenResponseDto? {
        val jwt = SignedJWT.parse(accessToken)
        val claims = jwt.jwtClaimsSet
        return appRefreshTokenRepo.findByTokenId(claims.jwtid)
                ?.let { refreshToken ->
                    try {
                        val tokenResponse = authServerClient.authenticateRefreshToken(refreshToken.refreshToken)
                        appRefreshTokenRepo.deleteById(refreshToken.id)
                        appRefreshTokenRepo.save(AppRefreshToken(0, tokenResponse.tokenId, tokenResponse.refreshToken))
                        tokenResponse
                    } catch (ex: Exception) {
                        log.debug("Error refreshing token", ex)
                        null
                    }
                }
    }

}
