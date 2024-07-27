package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import com.nimbusds.jose.jwk.JWK
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils

object DpopJwtCreator {

  fun create(privateKey: String, method: String, path: String): String {
    val publicKey = JWK.parse(privateKey).toPublicJWK()
    val headers = mapOf("typ" to "dpop+jwt", "jwk" to publicKey.toJSONObject())
    val payload =
        mapOf(
            "jti" to UUID.randomUUID().toString(),
            "htm" to method,
            "htu" to path,
            "iat" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
    return JoseUtils.sign(additionalHeaders = headers, payload = payload, privateKey = privateKey)
  }
}
