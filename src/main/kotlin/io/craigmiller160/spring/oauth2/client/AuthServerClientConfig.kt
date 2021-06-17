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

package io.craigmiller160.spring.oauth2.client

import io.craigmiller160.spring.oauth2.config.OAuthConfig
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthServerClientConfig (
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        private val oAuthConfig: OAuthConfig
) {

    @Bean
    fun formRestTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { template ->
            template.interceptors.add(io.craigmiller160.spring.oauth2.client.RequestResponseLoggingInterceptor())
        }
    }

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun authServerClient(restTemplateBuilder: RestTemplateBuilder): io.craigmiller160.spring.oauth2.client.AuthServerClient {
        return io.craigmiller160.spring.oauth2.client.AuthServerClientImpl(restTemplateBuilder.build(), oAuthConfig)
    }

}