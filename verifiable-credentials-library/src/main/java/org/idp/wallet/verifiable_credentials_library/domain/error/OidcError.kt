package org.idp.wallet.verifiable_credentials_library.domain.error

enum class OidcError(val error: String, val code: String, val description: String) {
  NOT_AUTHENTICATED("user_action", "2000", "User does not authenticate"),
  NOT_FOUND_REQUIRED_PARAMS(
      "system_error", "2001", "Authentication response must not contain required params.")
}
