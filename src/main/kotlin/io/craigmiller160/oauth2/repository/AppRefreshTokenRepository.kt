package io.craigmiller160.oauth2.repository

import io.craigmiller160.oauth2.entity.AppRefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface AppRefreshTokenRepository: JpaRepository<AppRefreshToken,Long> {

    fun findByTokenId(tokenId: String): AppRefreshToken?

    @Transactional
    @Modifying
    fun removeByTokenId(tokenId: String): Int

}
