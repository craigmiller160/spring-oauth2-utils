package io.craigmiller160.oauth2.repository

import io.craigmiller160.oauth2.entity.AppRefreshToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration

@DataJpaTest
@ContextConfiguration(classes = [
    AppRefreshTokenRepository::class
])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["io.craigmiller160.oauth2"])
@EntityScan(basePackages = ["io.craigmiller160.oauth2"])
class AppRefreshTokenRepositoryTest {

    @Autowired
    private lateinit var appRefreshTokenRepo: AppRefreshTokenRepository

    private lateinit var refreshToken1: AppRefreshToken
    private lateinit var refreshToken2: AppRefreshToken

    @BeforeEach
    fun setup() {
        refreshToken1 = AppRefreshToken(
                0, "1", "token1"
        )
        refreshToken1 = appRefreshTokenRepo.save(refreshToken1)

        refreshToken2 = AppRefreshToken(
                0, "2", "token2"
        )
        refreshToken2 = appRefreshTokenRepo.save(refreshToken2)
    }

    @AfterEach
    fun clean() {
        appRefreshTokenRepo.deleteAll()
    }

    @Test
    fun test_findByTokenId() {
        val result = appRefreshTokenRepo.findByTokenId("1")
        assertEquals(refreshToken1, result)
    }

    @Test
    fun test_removeByTokenId() {
        val result = appRefreshTokenRepo.removeByTokenId("1")
        Assertions.assertEquals(1, result)

        val results = appRefreshTokenRepo.findAll()
        Assertions.assertEquals(1, results.size)
        assertEquals(refreshToken2, results[0])
    }

}
