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
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/oauth")
class OAuthController (
        private val authCodeService: AuthCodeService,
        private val oAuthService: OAuth2Service
) {

    @PostMapping("/authcode/login")
    fun login(req: HttpServletRequest): AuthCodeLoginDto {
        val authCodeLoginUrl = authCodeService.prepareAuthCodeLogin(req)
        return AuthCodeLoginDto(authCodeLoginUrl)
    }

    @GetMapping("/authcode/code")
    fun code(@RequestParam("code") code: String, @RequestParam("state") state: String, req: HttpServletRequest, res: HttpServletResponse) {
        val (cookie, postAuthRedirect) = authCodeService.code(req, code, state)
        res.status = 302
        res.addHeader("Location", postAuthRedirect)
        res.addHeader("Set-Cookie", cookie)
    }

    @GetMapping("/logout")
    fun logout(res: HttpServletResponse) {
        val cookie = oAuthService.logout()
        res.addHeader("Set-Cookie", cookie)
    }

    @GetMapping("/user")
    fun getAuthenticatedUser(): AuthUserDto {
        return oAuthService.getAuthenticatedUser()
    }

}
