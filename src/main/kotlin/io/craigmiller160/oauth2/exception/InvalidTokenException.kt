package io.craigmiller160.oauth2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
class InvalidTokenException (msg: String, ex: Throwable? = null) : RuntimeException(msg, ex)
