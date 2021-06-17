package io.craigmiller160.spring.oauth2.dto

import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.spring.oauth2.security.AuthenticatedUser

// TODO see if this can eventually be migrated
fun authenticatedUserToAuthUserDto(authUser: AuthenticatedUser): AuthUserDto {
    return AuthUserDto(
            username = authUser.username,
            firstName = authUser.firstName,
            lastName = authUser.lastName,
            roles = authUser.authorities.map { it.authority }
    )
}