package org.idp.wallet.verifiable_credentials_library.domain.type.vc

import android.net.Uri

data class VerifiableCredentialsAuthorizationRequest(
    val issuer: String,
    val clientId: String,
    val scope: String,
    val redirectUri: String,
    val responseType: String = "code",
    val state: String? = null,
    val nonce: String? = null,
    val codeChallenge: String? = null,
    val codeChallengeMethod: CodeChallengeMethod? = null,
) {

  fun isResponseTypeCode(): Boolean {
    return responseType == "code"
  }

  fun queries(forceLogin: Boolean): String {
    val builder = Uri.Builder()
    builder.appendQueryParameter("client_id", clientId)
    builder.appendQueryParameter("scope", scope)
    builder.appendQueryParameter("response_type", responseType)
    builder.appendQueryParameter("redirect_uri", redirectUri)
    state?.let { builder.appendQueryParameter("state", it) }
    nonce?.let { builder.appendQueryParameter("nonce", it) }
    codeChallenge?.let { builder.appendQueryParameter("code_challenge", it) }
    codeChallengeMethod?.let {
      builder.appendQueryParameter("code_challenge_method", codeChallengeMethod.name)
    }
    //    if (forceLogin) {
    //      builder.appendQueryParameter("prompt", "login")
    //    }
    return builder.build().toString()
  }

  fun containsOidcInScope(): Boolean {
    return scope.contains("openid")
  }
}

enum class CodeChallengeMethod {
  plain,
  s256
}
