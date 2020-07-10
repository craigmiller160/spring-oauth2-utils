package io.craigmiller160.oauth2.config

import com.nimbusds.jose.jwk.JWKSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.net.URL
import javax.annotation.PostConstruct
import javax.validation.constraints.NotBlank

@Configuration
@Validated
@ConfigurationProperties(prefix = "oauth2")
data class OAuthConfig (
        @field:NotBlank(message = "Missing Property: oauth2.auth-server-host") var authServerHost: String = "",
        var authCodeRedirectUri: String = "",
        var clientName: String = "",
        var clientKey: String = "",
        var clientSecret: String = "",
        var cookieName: String = "",
        var postAuthRedirect: String = "",
        var cookieMaxAgeSecs: Long = 0
) {

    // TODO need validation that these properties are all set
    // TODO need to configure excludable paths

    val jwkPath = "/jwk"
    val tokenPath = "/oauth/token"
    val authCodeLoginPath = "/ui/login.html"

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    lateinit var jwkSet: JWKSet

    fun getBaseWait(): Long {
        return 1000
    }

    fun loadJWKSet(): JWKSet {
        return JWKSet.load(URL("$authServerHost$jwkPath"))
    }

    @PostConstruct
    fun tryToLoadJWKSet() {
        println("HOST: $authServerHost") // TODO delete this
        if (jwkPath.isNotBlank()) { // TODO once validation is added, this check isn't necessary
            for (i in 0 until 5) {
                try {
//                    jwkSet = loadJWKSet() // TODO restore this
                    return
                } catch (ex: Exception) {
                    log.error("Error loading JWKSet", ex)
                    Thread.sleep(getBaseWait() * (i + 1))
                }
            }

            throw RuntimeException("Failed to load JWKSet")
        }
    }

}
