/*
 * oauth2-utils
 * Copyright (C) 2020 Craig Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.spring.oauth2.repository

import io.craigmiller160.spring.oauth2.entity.JpaAppRefreshToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class JpaAppRefreshTokenRepositoryTest {

    @Autowired
    private lateinit var appRefreshTokenRepo: JpaAppRefreshTokenRepository

    private lateinit var refreshToken1: JpaAppRefreshToken
    private lateinit var refreshToken2: JpaAppRefreshToken

    @BeforeEach
    fun setup() {
        refreshToken1 = JpaAppRefreshToken(
                0, "1", "token1"
        )
        refreshToken1 = appRefreshTokenRepo.save(refreshToken1)

        refreshToken2 = JpaAppRefreshToken(
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
        appRefreshTokenRepo.removeByTokenId("1")

        val results = appRefreshTokenRepo.findAll()
        assertEquals(1, results.size)
        assertEquals(refreshToken2, results[0])
    }

}
