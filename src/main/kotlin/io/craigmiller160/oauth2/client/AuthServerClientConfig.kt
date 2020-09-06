package io.craigmiller160.oauth2.client

import io.craigmiller160.oauth2.config.OAuthConfig
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.boot.web.client.RestTemplateRequestCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.util.stream.Collectors

@Configuration
class AuthServerClientConfig (
        private val oAuthConfig: OAuthConfig
) {

    @Bean(name = ["authRestTemplateBuilder"])
    fun authRestTemplateBuilder(messageConverters: ObjectProvider<HttpMessageConverters>, customizers: ObjectProvider<RestTemplateCustomizer>, reqCustomizers: ObjectProvider<RestTemplateRequestCustomizer<*>>): RestTemplateBuilder {
        println("Working") // TODO delete this
        val uniqueConverters = messageConverters.ifUnique?.converters ?: listOf<HttpMessageConverter<*>>()
        return RestTemplateBuilder()
//                .requestFactory { BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory()) }
                .interceptors(listOf(RequestResponseLoggingInterceptor()))
                .messageConverters(FormHttpMessageConverter(), *uniqueConverters.toTypedArray())
                .customizers(*customizers.orderedStream().collect(Collectors.toList()).toTypedArray())
                .requestCustomizers(*reqCustomizers.orderedStream().collect(Collectors.toList()).toTypedArray())
    }

    @Bean
    fun authServerClient(restTemplate: RestTemplate): AuthServerClient {
        return AuthServerClientImpl(restTemplate, oAuthConfig)
    }

}
