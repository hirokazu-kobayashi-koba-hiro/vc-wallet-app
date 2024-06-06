package org.idp.wallet.verifiable_credentials_library.domain.error

import java.lang.RuntimeException

class OAuthBadRequestException(
    private val error: OAuthError,
    private val additionalDescription: String? = null,
    cause: Throwable? = null
) : VerifiableCredentialsError, RuntimeException(error.description, cause) {

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
