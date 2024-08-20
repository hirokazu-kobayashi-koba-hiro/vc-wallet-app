package org.idp.wallet.verifiable_credentials_library.domain.error

import java.lang.RuntimeException

class OpenIdConnectException(
    private val error: OidcError,
    private val additionalDescription: String? = null,
    initialCause: Throwable? = null
) : VerifiableCredentialsError, RuntimeException(error.description, initialCause) {

  fun error(): String {
    return error.error
  }

  override fun code(): String {
    return error.code
  }

  override fun description(): String {
    if (additionalDescription != null) {
      return error.description + " " + additionalDescription
    }
    return error.description
  }

  override fun cause(): Throwable? {
    return cause
  }
}
