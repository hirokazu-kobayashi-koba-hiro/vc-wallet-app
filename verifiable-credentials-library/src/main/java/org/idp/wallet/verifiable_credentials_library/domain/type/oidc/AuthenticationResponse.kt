package org.idp.wallet.verifiable_credentials_library.domain.type.oidc

class AuthenticationResponse(private val values: Map<String, String>) {

  fun code(): String {
    return values["code"] ?: ""
  }

  fun issuer(): String? {
    return values["iss"]
  }

  fun error(): String? {
    return values["error"]
  }

  fun errorDescription(): String? {
    return values["error_description"]
  }
}
