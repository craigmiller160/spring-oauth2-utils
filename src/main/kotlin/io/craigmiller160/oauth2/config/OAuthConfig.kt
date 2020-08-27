package io.craigmiller160.oauth2.config

import com.nimbusds.jose.jwk.JWKSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.net.URL
import javax.annotation.PostConstruct
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Configuration
@Validated
@ConfigurationProperties(prefix = "oauth2")
data class OAuthConfig (
        @field:NotBlank(message = "Missing Property: oauth2.auth-server-host") var authServerHost: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.auth-code-redirect-uri") var authCodeRedirectUri: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-name") var clientName: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-key") var clientKey: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.client-secret") var clientSecret: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.cookie-name") var cookieName: String = "",
        @field:NotBlank(message = "Missing Property: oauth2.post-auth-redirect") var postAuthRedirect: String = "",
        @field:Min(message = "Must be greater than 0: oauth2.cookie-max-age-secs", value = 1) var cookieMaxAgeSecs: Long = 0,
        var useOriginForRedirect: Boolean = false,
        var internalAuthServerHost: String = authServerHost,
        var insecurePaths: String = "",
        var authCodeWaitMins: Long = 10
) {

    val jwkPath = "/jwk"
    val tokenPath = "/oauth/token"
    val authCodeLoginPath = "/ui/login.html"

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    lateinit var jwkSet: JWKSet

    fun getBaseWait(): Long {
        return 1000
    }

    fun loadJWKSet(): JWKSet {
        return JWKSet.load(URL("$internalAuthServerHost$jwkPath"))
    }

    fun getInsecurePathList(): List<String> {
        return insecurePaths.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
    }

    @PostConstruct
    fun tryToLoadJWKSet() {
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
