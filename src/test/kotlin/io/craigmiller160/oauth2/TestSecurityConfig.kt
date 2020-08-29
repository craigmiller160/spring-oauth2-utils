package io.craigmiller160.oauth2

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class TestSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http?.let {
            it.csrf().disable()
                    .authorizeRequests().anyRequest().permitAll()
        }
    }

}
