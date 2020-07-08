package io.craigmiller160.oauth2.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthenticatedUser (
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
