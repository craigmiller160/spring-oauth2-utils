package io.craigmiller160.spring.oauth2.controller

import io.craigmiller160.oauth2.exception.BadAuthenticationException
import io.craigmiller160.spring.oauth2.exception.SpringBadAuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandlingAdvice {

    // TODO need to validate that this actually accomplishes its goal
    @ExceptionHandler(BadAuthenticationException::class)
    fun handleBadAuthenticationException(ex: BadAuthenticationException) {
        throw SpringBadAuthenticationException(ex)
    }

}