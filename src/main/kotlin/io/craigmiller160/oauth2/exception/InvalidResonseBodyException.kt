package io.craigmiller160.oauth2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Invalid response body")
class InvalidResponseBodyException : RuntimeException()
