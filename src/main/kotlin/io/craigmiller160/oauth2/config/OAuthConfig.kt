package io.craigmiller160.oauth2.config

import com.nimbusds.jose.jwk.JWKSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.net.URL
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties(prefix = "oauth2")
data class OAuthConfig (
        var authServerHost: String = "",
        var authCodeRedirectUri: String = "",
        var clientName: String = "",
        var clientKey: String = "",
        var clientSecret: String = "",
        var cookieName: String = "",
        var postAuthRedirect: String = "",
        var cookieMaxAgeSecs: Long = 0
) {

    // TODO need validation that these properties are all set

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
        if (jwkPath.isNotBlank()) { // TODO add this to unit tests
            for (i in 0 until 5) {
                try {
                    jwkSet = loadJWKSet()
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
