package org.idp.wallet.verifiable_credentials_library.domain.error

import java.lang.RuntimeException

class SettingInvalidException(val error: SettingError, cause: Throwable? = null) :
    VerifiableCredentialsError, RuntimeException(error.description, cause) {
  override fun code(): String {
    return error.code
  }

  override fun description(): String {
    return error.description
  }

  override fun cause(): Throwable? {
    return cause
  }
}
