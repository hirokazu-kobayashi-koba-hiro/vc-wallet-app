package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import org.idp.wallet.verifiable_credentials_library.util.date.DateUtils
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils

class CredentialRequestProofCreator(
    private val cNonce: String?,
    private val clientId: String,
    private val issuer: String,
    private val privateKey: String
) {

  fun create(): Map<String, Any> {
    val header = mapOf("" to "")
    val payload =
        mutableMapOf("iss" to clientId, "aud" to issuer, "iat" to DateUtils.nowAsEpochSecond())
    cNonce?.let { payload.put("nonce", it) }
    val jwt = JoseUtils.sign(additionalHeaders = header, payload = payload, privateKey = privateKey)
    return mapOf("proof_type" to "jwt", "proof" to jwt)
  }
}
