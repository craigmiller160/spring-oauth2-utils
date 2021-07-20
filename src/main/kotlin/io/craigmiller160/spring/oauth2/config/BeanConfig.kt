package io.craigmiller160.spring.oauth2.config

import io.craigmiller160.oauth2.client.AuthServerClient
import io.craigmiller160.oauth2.client.AuthServerClientImpl
import io.craigmiller160.oauth2.config.OAuth2Config
import io.craigmiller160.oauth2.domain.repository.AppRefreshTokenRepository
import io.craigmiller160.oauth2.domain.repository.impl.AppRefreshTokenRepositoryImpl
import io.craigmiller160.oauth2.security.AuthenticatedUser
import io.craigmiller160.oauth2.security.CookieCreator
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuth2Service
import io.craigmiller160.oauth2.service.RefreshTokenService
import io.craigmiller160.oauth2.service.impl.AuthCodeServiceImpl
import io.craigmiller160.oauth2.service.impl.OAuth2ServiceImpl
import io.craigmiller160.oauth2.service.impl.RefreshTokenServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import javax.sql.DataSource

@Configuration
class BeanConfig {
    @Bean
    fun cookieCreator(): CookieCreator = CookieCreator()

    @Bean
    fun authServerClient(oAuth2Config: OAuth2Config): AuthServerClient {
        return AuthServerClientImpl(oAuth2Config)
    }

    @Bean
    fun appRefreshTokenRepository(oAuth2Config: OAuth2Config, dataSource: DataSource): AppRefreshTokenRepository {
        return AppRefreshTokenRepositoryImpl(oAuth2Config) { dataSource.connection }
    }

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

    @Bean
    fun refreshTokenService(appRefreshTokenRepo: AppRefreshTokenRepository, authServerClient: AuthServerClient): RefreshTokenService {
        return RefreshTokenServiceImpl(appRefreshTokenRepo, authServerClient)
    }

    @Bean
    fun authCodeService(oAuth2Config: OAuth2Config,
                        authServerClient: AuthServerClient,
                        appRefreshTokenRepo: AppRefreshTokenRepository,
                        cookieCreator: CookieCreator): AuthCodeService {
        return AuthCodeServiceImpl(oAuth2Config, authServerClient, appRefreshTokenRepo, cookieCreator)
    }
}