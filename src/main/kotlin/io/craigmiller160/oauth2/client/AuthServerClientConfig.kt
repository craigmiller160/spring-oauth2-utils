package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.config.OAuthConfig
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class AuthServerClientConfig (
        private val oAuthConfig: OAuthConfig
) {

    @Bean
    fun authRestTemplateBuilder(): RestTemplateBuilder {
        return RestTemplateBuilder()
//                .requestFactory { BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory()) }
                .interceptors(listOf(RequestResponseLoggingInterceptor()))
                .messageConverters(FormHttpMessageConverter(), MappingJackson2HttpMessageConverter())
    }

    @Bean
    fun authServerClient(restTemplate: RestTemplate): AuthServerClient {
        return AuthServerClientImpl(restTemplate, oAuthConfig)
    }

}
