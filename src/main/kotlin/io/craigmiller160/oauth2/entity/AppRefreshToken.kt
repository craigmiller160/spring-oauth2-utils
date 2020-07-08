package io.craigmiller160.oauth2.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "app_refresh_tokens")
data class AppRefreshToken (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val tokenId: String,
        val refreshToken: String
)
