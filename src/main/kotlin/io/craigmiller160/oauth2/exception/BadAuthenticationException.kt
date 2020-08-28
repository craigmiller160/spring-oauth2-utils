package io.craigmiller160.oauth2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Bad authentication")
class BadAuthenticationException(msg: String, cause: Throwable?) : RuntimeException(msg, cause)
