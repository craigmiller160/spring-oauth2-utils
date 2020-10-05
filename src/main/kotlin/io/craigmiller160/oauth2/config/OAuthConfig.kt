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

package io.craigmiller160.oauth2.config

import com.nimbusds.jose.jwk.JWKSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.net.URL
import javax.annotation.PostConstruct
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Configuration
@Validated
@ConfigurationProperties(prefix = "oauth2")
data class OAuthConfig (
        @field:NotBlank(message = "Missing Property: oauth2.auth-server-host") var authServerHost: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.auth-code-redirect-uri") var authCodeRedirectUri: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-name") var clientName: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-key") var clientKey: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-secret") var clientSecret: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.cookie-name") var cookieName: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.post-auth-redirect") var postAuthRedirect: String = "",
        @field:Min(message = "Must be greater than 0: oauth2.cookie-max-age-secs", value = 1) var cookieMaxAgeSecs: Long = 0,
        @field:NotBlank(message = "Missing Property: oauth2.auth-login-base-uri") var authLoginBaseUri: String = "",
        var insecurePaths: String = "",
        var authCodeWaitMins: Long = 10
) {

    val jwkPath = "/jwk"
    val tokenPath = "/oauth/token"
    val authCodeLoginPath = "/ui/login"

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    lateinit var jwkSet: JWKSet

    fun getBaseWait(): Long {
        return 1000
    }

    fun loadJWKSet(): JWKSet {
        return JWKSet.load(URL("$authServerHost$jwkPath"))
    }

    fun getInsecurePathList(): List<String> {
        return insecurePaths.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
    }

    @PostConstruct
    fun tryToLoadJWKSet() {
        for (i in 0 until 5) {
            try {
                jwkSet = loadJWKSet()
                return
            } catch (ex: Exception) {
                log.error("Error loading JWKSet", ex)
                Thread.sleep(getBaseWait() * (i + 1))
            }
        }

        throw RuntimeException("Failed to load JWKSet")
    }

}
