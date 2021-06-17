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

package io.craigmiller160.spring.oauth2.config

import com.nimbusds.jose.jwk.JWKSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.spy

class OAuth2ConfigImplTest {

    private val oAuthConfig = OAuth2ConfigImpl()

    @Test
    fun test_loadJWKSet_firstTrySuccess() {
        val spyConfig = spy(oAuthConfig)

        doReturn(1L)
                .`when`(spyConfig)
                .getBaseWait()

        doReturn(Mockito.mock(JWKSet::class.java))
                .`when`(spyConfig)
                .loadJWKSet()

        spyConfig.tryToLoadJWKSet()
        assertNotNull(spyConfig.jwkSet)
    }

    @Test
    fun test_loadJWKSet_secondTrySuccess() {
        val spyConfig = spy(oAuthConfig)

        doReturn(1L)
                .`when`(spyConfig)
                .getBaseWait()

        doThrow(RuntimeException("Hello"))
                .doReturn(Mockito.mock(JWKSet::class.java))
                .`when`(spyConfig)
                .loadJWKSet()

        spyConfig.tryToLoadJWKSet()
        assertNotNull(spyConfig.jwkSet)
    }

    @Test
    fun test_loadJWKSet_failure() {
        val spyConfig = spy(oAuthConfig)

        doReturn(1L)
                .`when`(spyConfig)
                .getBaseWait()

        doThrow(RuntimeException("Hello"))
                .`when`(spyConfig)
                .loadJWKSet()

        val ex = assertThrows<java.lang.RuntimeException> { spyConfig.tryToLoadJWKSet() }
        assertEquals("Failed to load JWKSet", ex.message)
        val ex2 = assertThrows<UninitializedPropertyAccessException> { spyConfig.jwkSet }
    }

}
