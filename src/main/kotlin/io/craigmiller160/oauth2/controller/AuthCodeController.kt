package io.craigmiller160.oauth2.controller

import io.craigmiller160.oauth2.service.AuthCodeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/authcode")
class AuthCodeController (
        private val authCodeService: AuthCodeService
) {

    @GetMapping("/login")
    fun login(req: HttpServletRequest, res: HttpServletResponse) {
        val authCodeLoginUrl = authCodeService.prepareAuthCodeLogin(req)
        res.status = 302
        res.addHeader("Location", authCodeLoginUrl)
    }

    @GetMapping("/code")
    fun code(@RequestParam("code") code: String, @RequestParam("state") state: String, req: HttpServletRequest, res: HttpServletResponse) {
        val (cookie, postAuthRedirect) = authCodeService.code(req, code, state)
        res.status = 302
        res.addHeader("Location", postAuthRedirect)
        res.addHeader("Set-Cookie", cookie.toString())
    }

    @GetMapping("/logout")
    fun logout(res: HttpServletResponse) {
        val cookie = authCodeService.logout()
        res.addHeader("Set-Cookie", cookie.toString())
    }

}
