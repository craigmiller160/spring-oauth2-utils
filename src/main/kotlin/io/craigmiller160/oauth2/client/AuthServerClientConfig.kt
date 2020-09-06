package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.config.OAuthConfig
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthServerClientConfig (
        private val oAuthConfig: OAuthConfig
) {

    // TODO update rest template docs

    @Bean
    fun formRestTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { template ->
            template.interceptors.add(RequestResponseLoggingInterceptor())
        }
    }

    @Bean
    fun authServerClient(restTemplateBuilder: RestTemplateBuilder): AuthServerClient {
        return AuthServerClientImpl(restTemplateBuilder.build(), oAuthConfig)
    }

}
