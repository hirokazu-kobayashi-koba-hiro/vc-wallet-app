package org.idp.wallet.verifiable_credentials_library.domain.error

enum class VcError(val error: String, val code: String, val description: String) {
  NOT_FOUND_REQUIRED_PARAMS(
      "invalid_request", "3000", "Authorization request must not contain required params."),
  DUPLICATE_KEY(
      "invalid_request", "3001", "Authorization request must not contain duplicate value."),
  NOT_AUTHENTICATED("user_action", "3002", "User does not authenticate"),
  UNSUPPORTED_DEFERRED_CREDENTIAL("unsupported_error", "3003", "Unsupported deferred credential"),
  UNSUPPORTED_CREDENTIAL_FORMAT("unsupported_error", "3004", "Unsupported format of credential")
}
