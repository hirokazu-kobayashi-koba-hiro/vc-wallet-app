package org.idp.wallet.verifiable_credentials_library.domain.error

import java.lang.RuntimeException

class NetworkException(val code: String, val description: String, cause: Throwable? = null) :
    VerifiableCredentialsError, RuntimeException(description, cause) {
  override fun code(): String {
    return code
  }

  override fun description(): String {
    return description
  }

  override fun cause(): Throwable? {
    return cause
  }
}
