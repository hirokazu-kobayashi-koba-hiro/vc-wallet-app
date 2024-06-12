package org.idp.wallet.verifiable_credentials_library.domain.type.oauth

data class TokenRequest(
    val clientId: String,
    val grantType: String,
    val code: String? = null,
    val redirectUri: String? = null,
    val refreshToken: String? = null,
    val codeVerifier: String? = null,
) {

  fun values(): Map<String, String> {
    val values = mutableMapOf<String, String>()
    values["client_id"] = clientId
    values["grant_type"] = grantType
    code?.let { values["code"] = it }
    redirectUri?.let { values["redirect_uri"] = it }
    refreshToken?.let { values["refresh_token"] = it }
    codeVerifier?.let { values["code_verifier"] = it }
    return values
  }
}
