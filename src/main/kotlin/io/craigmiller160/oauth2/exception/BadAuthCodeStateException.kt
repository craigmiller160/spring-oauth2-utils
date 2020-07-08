package io.craigmiller160.oauth2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Invalid Auth Code State")
class BadAuthCodeStateException(msg: String) : RuntimeException(msg)
