package io.craigmiller160.oauth2.util

import org.springframework.http.ResponseCookie

object CookieCreator {

    fun create(name: String, token: String, maxAge: Long): ResponseCookie {
        return ResponseCookie
                .from(name, token)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("strict")
                .build()
    }

}
