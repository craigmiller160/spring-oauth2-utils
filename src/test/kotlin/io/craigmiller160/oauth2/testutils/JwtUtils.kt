package io.craigmiller160.oauth2.testutils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.oauth2.security.AuthenticatedUser
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object JwtUtils {

    const val ROLE_1 = "ROLE_1"
    const val ROLE_2 = "ROLE_2"
    const val USERNAME = "username"
    const val ROLES_CLAIM = "roles"
    const val CLIENT_KEY = "clientKey"
    const val CLIENT_NAME = "clientName"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val TOKEN_ID = "JWTID"

    fun createKeyPair(): KeyPair {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        return keyPairGen.genKeyPair()
    }

    fun createJwkSet(keyPair: KeyPair): JWKSet {
        val builder = RSAKey.Builder(keyPair.public as RSAPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("oauth-jwt")
        return JWKSet(builder.build())
    }

    fun createAuthUser(): AuthenticatedUser {
        return AuthenticatedUser(
                userName = USERNAME,
                grantedAuthorities = listOf(SimpleGrantedAuthority(ROLE_1), SimpleGrantedAuthority(ROLE_2)),
                firstName = FIRST_NAME,
                lastName = LAST_NAME,
                tokenId = TOKEN_ID
        )
    }

    fun createJwt(expMinutes: Long = 100): SignedJWT {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .build()

        val exp = LocalDateTime.now().plusMinutes(expMinutes)
        val expDate = Date.from(exp.atZone(ZoneId.systemDefault()).toInstant())

        val claims = JWTClaimsSet.Builder()
                .jwtID(TOKEN_ID)
                .issueTime(Date())
                .subject(USERNAME)
                .expirationTime(expDate)
                .claim(ROLES_CLAIM, listOf(ROLE_1, ROLE_2))
                .claim("clientKey", CLIENT_KEY)
                .claim("clientName", CLIENT_NAME)
                .claim("firstName", FIRST_NAME)
                .claim("lastName", LAST_NAME)
                .build()
        return SignedJWT(header, claims)
    }

    fun signAndSerializeJwt(jwt: SignedJWT, privateKey: PrivateKey): String {
        val signer = RSASSASigner(privateKey)
        jwt.sign(signer)
        return jwt.serialize()
    }

}
