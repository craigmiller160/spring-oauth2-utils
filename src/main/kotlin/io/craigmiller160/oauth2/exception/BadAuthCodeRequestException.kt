package io.craigmiller160.oauth2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Bad auth code request")
class BadAuthCodeRequestException(msg: String) : RuntimeException(msg)
