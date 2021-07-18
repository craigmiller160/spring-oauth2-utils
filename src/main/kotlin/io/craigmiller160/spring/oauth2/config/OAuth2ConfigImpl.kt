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

package io.craigmiller160.spring.oauth2.config

import io.craigmiller160.oauth2.config.AbstractOAuth2Config
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Configuration
@Validated
@ConfigurationProperties(prefix = "oauth2")
class OAuth2ConfigImpl : AbstractOAuth2Config() {

    @field:NotBlank(message = "Missing Property: oauth2.auth-server-host")
    override var authServerHost: String = ""
    override var authCodeRedirectUri: String = ""
    @field:NotBlank(message = "Missing Property: oauth2.client-name")
    override var clientName: String = ""
    @field:NotBlank(message = "Missing Property: oauth2.client-key")
    override var clientKey: String = ""
    @field:NotBlank(message = "Missing Property: oauth2.client-secret")
    override var clientSecret: String = ""
    override var cookieName: String = ""
    override var postAuthRedirect: String = ""
    override var cookieMaxAgeSecs: Long = 0
    override var cookiePath: String = ""
    override var authLoginBaseUri: String = ""
    override var insecurePaths: String = ""
    override var authCodeWaitMins: Long = 10
    override var schemaName: String = ""

}
