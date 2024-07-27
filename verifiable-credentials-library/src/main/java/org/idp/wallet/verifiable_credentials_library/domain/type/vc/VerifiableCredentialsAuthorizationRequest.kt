package org.idp.wallet.verifiable_credentials_library.domain.type.vc

import android.net.Uri

data class VerifiableCredentialsAuthorizationRequest(
    val issuer: String,
    val requestUri: String? = null,
    val clientId: String? = null,
    val scope: String? = null,
    val redirectUri: String? = null,
    val responseType: String? = null,
    val state: String? = null,
    val nonce: String? = null,
    val codeChallenge: String? = null,
    val codeChallengeMethod: CodeChallengeMethod? = null,
) {

  fun isResponseTypeCode(): Boolean {
    return responseType == "code"
  }

  fun queries(): String {
    val builder = Uri.Builder()
    requestUri?.let { builder.appendQueryParameter("request_uri", requestUri) }
    clientId?.let { builder.appendQueryParameter("client_id", clientId) }
    scope?.let { builder.appendQueryParameter("scope", scope) }
    responseType?.let { builder.appendQueryParameter("response_type", responseType) }
    redirectUri?.let { builder.appendQueryParameter("redirect_uri", redirectUri) }
    state?.let { builder.appendQueryParameter("state", it) }
    nonce?.let { builder.appendQueryParameter("nonce", it) }
    codeChallenge?.let { builder.appendQueryParameter("code_challenge", it) }
    codeChallengeMethod?.let {
      builder.appendQueryParameter("code_challenge_method", codeChallengeMethod.name)
    }
    return builder.build().toString()
  }

  fun containsOidcInScope(): Boolean {
    return scope?.contains("openid") ?: false
  }
}

enum class CodeChallengeMethod {
  plain,
  s256
}
