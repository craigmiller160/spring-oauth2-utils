package io.craigmiller160.oauth2.controller

import io.craigmiller160.oauth2.dto.AuthCodeLoginDto
import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.AuthCodeService
import io.craigmiller160.oauth2.service.OAuthService
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
        private val oAuthService: OAuthService
) {

    // TODO need to completely overhaul documentation for this
    @PostMapping("/authcode/login")
    fun login(req: HttpServletRequest): AuthCodeLoginDto {
        println("WORKING") // TODO delete this
        val authCodeLoginUrl = authCodeService.prepareAuthCodeLogin(req)
        return AuthCodeLoginDto(authCodeLoginUrl)
    }

    @GetMapping("/authcode/code")
    fun code(@RequestParam("code") code: String, @RequestParam("state") state: String, req: HttpServletRequest, res: HttpServletResponse) {
        val (cookie, postAuthRedirect) = authCodeService.code(req, code, state)
        res.status = 302
        res.addHeader("Location", postAuthRedirect)
        res.addHeader("Set-Cookie", cookie.toString())
    }

    @GetMapping("/logout")
    fun logout(res: HttpServletResponse) {
        val cookie = oAuthService.logout()
        res.addHeader("Set-Cookie", cookie.toString())
    }

    @GetMapping("/user")
    fun getAuthenticatedUser(): AuthUserDto {
        return oAuthService.getAuthenticatedUser()
    }

}
