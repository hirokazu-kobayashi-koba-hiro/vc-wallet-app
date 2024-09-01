package org.idp.wallet.verifiable_credentials_library.domain.error

enum class OidcError(val error: String, val code: String, val description: String) {
  NOT_AUTHENTICATED("user_action", "2000", "User does not authenticate"),
  NOT_FOUND_REQUIRED_PARAMS(
      "system_error", "2001", "Authentication response must not contain required params."),
  INVALID_ID_TOKEN("system_error", "2002", "IdToken is inValid."),
  UNSUPPORTED_OPERATION("system_error", "2090", "Unsupported operation."),
  UNEXPECTED_ERROR("system_error", "2099", "Unexpected error.")
}
