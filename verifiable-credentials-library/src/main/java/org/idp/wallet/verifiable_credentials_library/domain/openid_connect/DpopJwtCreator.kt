package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import com.nimbusds.jose.jwk.JWK
import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.util.date.DateUtils
import org.idp.wallet.verifiable_credentials_library.util.hash.calculateHashWithSha256
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils

object DpopJwtCreator {

  fun create(
      privateKey: String,
      method: String,
      path: String,
      accessToken: String? = null
  ): String {
    val publicKey = JWK.parse(privateKey).toPublicJWK()
    val headers = mapOf("typ" to "dpop+jwt", "jwk" to publicKey.toJSONObject())
    val payload =
        mutableMapOf(
            "jti" to UUID.randomUUID().toString(),
            "htm" to method,
            "htu" to path,
            "iat" to DateUtils.nowAsEpochSecond(),
        )
    accessToken?.let { payload.put("ath", calculateHashWithSha256(accessToken)) }
    return JoseUtils.sign(additionalHeaders = headers, payload = payload, privateKey = privateKey)
  }
}
