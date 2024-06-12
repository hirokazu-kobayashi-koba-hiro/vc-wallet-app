package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import java.time.LocalDateTime
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse

class TokenRecord(
    val tokenResponse: TokenResponse,
    private val refreshTokenExpiresIn: Long,
    private val createdAt: LocalDateTime = LocalDateTime.now()
) {

  fun refresh(tokenResponse: TokenResponse): TokenRecord {
    return TokenRecord(tokenResponse, refreshTokenExpiresIn, createdAt)
  }

  fun isExpiredAccessToken(now: LocalDateTime, graceDuration: Long = 10): Boolean {
    return createdAt.plusSeconds(tokenResponse.expiresIn - graceDuration).isBefore(now)
  }

  fun isExpiredRefreshToken(now: LocalDateTime, graceDuration: Long = 10): Boolean {
    return createdAt.plusSeconds(refreshTokenExpiresIn - graceDuration).isBefore(now)
  }
}
