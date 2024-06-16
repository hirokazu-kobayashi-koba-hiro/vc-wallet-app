package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import java.time.LocalDateTime

class TokenDirector(
    private val force: Boolean,
    private val tokenRecord: TokenRecord?,
) {

  fun direct(): TokenDirection {
    if (force || tokenRecord == null) {
      return TokenDirection.ISSUE
    }
    val now = LocalDateTime.now()
    if (!tokenRecord.isExpiredAccessToken(now)) {
      return TokenDirection.CACHE
    }
    tokenRecord.tokenResponse.refreshToken?.let {
      if (tokenRecord.isExpiredAccessToken(now) && !tokenRecord.isExpiredRefreshToken(now)) {
        return TokenDirection.REFRESH
      }
    }
    return TokenDirection.ISSUE
  }
}

enum class TokenDirection {
  ISSUE,
  REFRESH,
  CACHE,
}
