package io.craigmiller160.oauth2.service

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
        return CookieCreator.create(oAuthConfig.cookieName, "", 0)
    }

    fun getAuthenticatedUser(): AuthUserDto {
        val authUser = SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
        return AuthUserDto.fromAuthenticatedUser(authUser)
    }

}
