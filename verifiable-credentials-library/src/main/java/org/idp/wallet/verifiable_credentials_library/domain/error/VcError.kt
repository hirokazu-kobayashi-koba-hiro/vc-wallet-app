package org.idp.wallet.verifiable_credentials_library.domain.error

enum class VcError(val error: String, val code: String, val description: String) {
  NOT_INITIALIZED("illegal_implementation", "3000", "VerifiableCredentialsApi is not initialized"),
  NOT_FOUND_REQUIRED_PARAMS(
      "invalid_request", "3001", "Authorization request must not contain required params."),
  DUPLICATE_KEY(
      "invalid_request", "3002", "Authorization request must not contain duplicate value."),
  NOT_AUTHENTICATED("user_action", "3003", "User does not authenticate"),
  UNSUPPORTED_DEFERRED_CREDENTIAL("unsupported_error", "3004", "Unsupported deferred credential"),
  UNSUPPORTED_CREDENTIAL_FORMAT("unsupported_error", "3005", "Unsupported format of credential"),
  INVALID_VC_ISSUER_METADATA("issuer_invalid_metadata", "3006", "invalid vc issuer metadata"),
  VC_ISSUER_UNSUPPORTED_DYNAMIC_CLIENT_REGISTRATION(
      "issuer_unsupported_error", "3007", "issuer is unsupported dynamic client registration"),
  UNSUPPORTED_OPERATION("system_error", "3090", "Unsupported operation."),
}
