package io.craigmiller160.spring.oauth2.config

import io.craigmiller160.oauth2.config.OAuth2Config
import io.craigmiller160.oauth2.domain.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.security.AuthenticatedUser
import io.craigmiller160.oauth2.security.CookieCreator
import io.craigmiller160.oauth2.service.OAuth2Service
import io.craigmiller160.oauth2.service.OAuth2ServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
class BeanConfig {
    @Bean
    fun cookieCreator(): CookieCreator = CookieCreator()

    @Bean
    fun oAuth2Service(
            oAuth2Config: OAuth2Config,
            appRefreshTokenRepo: AppRefreshTokenRepository,
            cookieCreator: CookieCreator
    ): OAuth2Service = OAuth2ServiceImpl(
            oAuth2Config,
            appRefreshTokenRepo,
            cookieCreator
    ) {
        SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
    }
}