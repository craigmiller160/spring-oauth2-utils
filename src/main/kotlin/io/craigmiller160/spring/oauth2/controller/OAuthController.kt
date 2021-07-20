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

package io.craigmiller160.spring.oauth2.controller

import io.craigmiller160.oauth2.dto.AuthCodeLoginDto
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.endpoint.OAuth2Endpoint
import io.craigmiller160.oauth2.endpoint.PathConstants
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(PathConstants.ROOT_PATH)
class OAuthController (
        private val authCodeService: AuthCodeService,
        private val oAuthService: OAuth2Service
) : OAuth2Endpoint<ResponseEntity<*>> {

    @Autowired
    private lateinit var req: HttpServletRequest

    @PostMapping(PathConstants.AUTHCODE_LOGIN_PATH)
    override fun login(): ResponseEntity<AuthCodeLoginDto> {
        val authCodeLoginUrl = authCodeService.prepareAuthCodeLogin(req)
        return ResponseEntity.ok(AuthCodeLoginDto(authCodeLoginUrl))
    }

    @GetMapping(PathConstants.AUTHCODE_CODE_PATH)
    override fun code(@RequestParam("code") code: String, @RequestParam("state") state: String): ResponseEntity<Void> {
        val (cookie, postAuthRedirect) = authCodeService.code(req, code, state)
        return ResponseEntity.status(302)
                .header("Location", postAuthRedirect)
                .header("Set-Cookie", cookie)
                .build()
    }

    @GetMapping(PathConstants.LOGOUT_PATH)
    override fun logout(): ResponseEntity<Void> {
        val cookie = oAuthService.logout()
        return ResponseEntity.status(200)
                .header("Set-Cookie", cookie)
                .build()
    }

    @GetMapping(PathConstants.AUTH_USER_PATH)
    override fun getAuthenticatedUser(): ResponseEntity<AuthUserDto> {
        val authUser = oAuthService.getAuthenticatedUser()
        return ResponseEntity.ok(authUser)
    }

}
