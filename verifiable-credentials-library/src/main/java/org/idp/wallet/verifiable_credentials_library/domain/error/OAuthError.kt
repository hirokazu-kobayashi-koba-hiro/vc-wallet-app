package org.idp.wallet.verifiable_credentials_library.domain.error

enum class OAuthError(val error: String, val code: String, val description: String) {
  NOT_FOUND_REQUIRED_PARAMS(
      "invalid_request", "1000", "Authorization request must not contain required params."),
  DUPLICATE_KEY(
      "invalid_request", "1001", "Authorization request must not contain duplicate value.")
}
