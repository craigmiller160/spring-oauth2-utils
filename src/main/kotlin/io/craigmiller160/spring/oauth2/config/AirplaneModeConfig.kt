package io.craigmiller160.spring.oauth2.config

import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.spring.oauth2.security.AuthenticatedUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.util.UUID
import kotlin.math.round

@Configuration
class AirplaneModeConfig(
    @Value("\${spring.profiles.active}")
    private val activeProfiles: String
) {
    companion object {
        private const val USER_ID = 1L
        private const val USERNAME = "default@gmail.com"
        private const val FIRST_NAME = "Default"
        private const val LAST_NAME = "User"
        private val ROLES = listOf<String>()

        val AUTH_USER = AuthUserDto(
            userId = USER_ID,
            username = USERNAME,
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            roles = ROLES
        )

        val AUTHENTICATION = AuthenticatedUserDetails(
            userId = USER_ID,
            userName = USERNAME,
            roles = ROLES,
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            tokenId = UUID.randomUUID().toString()
        )
    }

    fun isAirplaneMode(): Boolean = activeProfiles.contains("airplane")
}