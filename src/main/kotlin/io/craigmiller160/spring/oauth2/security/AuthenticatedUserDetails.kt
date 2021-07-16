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

package io.craigmiller160.spring.oauth2.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthenticatedUserDetails (
        private val userName: String,
        private val grantedAuthorities: List<GrantedAuthority>,
        val firstName: String,
        val lastName: String,
        val tokenId: String
): UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.grantedAuthorities.toMutableList()
    }

    override fun getUsername(): String {
        return this.userName
    }

    override fun isEnabled() = true
    override fun isCredentialsNonExpired() = true
    override fun getPassword() = ""
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true

}
