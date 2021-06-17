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

import io.craigmiller160.oauth2.config.OAuthConfig
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.security.AuthenticatedUser
import io.craigmiller160.oauth2.util.CookieCreator
import org.springframework.http.ResponseCookie
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class OAuthService (
        private val appRefreshTokenRepo: AppRefreshTokenRepository,
        private val oAuthConfig: OAuthConfig
) {

    fun logout(): ResponseCookie {
        val authUser = SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
        appRefreshTokenRepo.removeByTokenId(authUser.tokenId)
        return CookieCreator.create(oAuthConfig.cookieName, oAuthConfig.getOrDefaultCookiePath(), "", 0)
    }

    fun getAuthenticatedUser(): AuthUserDto {
        val authUser = SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
        return AuthUserDto.fromAuthenticatedUser(authUser)
    }

}
