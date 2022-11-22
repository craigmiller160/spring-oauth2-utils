package io.craigmiller160.spring.oauth2.config

import io.craigmiller160.oauth2.dto.AuthUserDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class AirplaneModeConfig(
    @Value("\${spring.profiles.active}")
    private val activeProfiles: String
) {
    companion object {
        val AIRPLANE_MODE_AUTH_USER = AuthUserDto(
            userId = 1L,
            username = "default@gmail.com",
            firstName = "Default",
            lastName = "User",
            roles = listOf()
        )
    }

    fun isAirplaneMode(): Boolean = activeProfiles.contains("airplane")
}