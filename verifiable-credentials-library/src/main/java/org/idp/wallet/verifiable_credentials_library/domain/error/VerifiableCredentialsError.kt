package org.idp.wallet.verifiable_credentials_library.domain.error

import android.util.Log

interface VerifiableCredentialsError {
  fun code(): String

  fun description(): String

  fun cause(): Throwable?
}

fun Exception.toVerifiableCredentialsError(): VerifiableCredentialsError {
  Log.e("VcWalletLibrary", this.message ?: "Unknown error", this)
  return when (this) {
    is VerifiableCredentialsException -> this
    is NetworkException -> this
    is OAuthBadRequestException -> this
    is SettingInvalidException -> this
    else -> VerifiableCredentialsException(VcError.UNKNOWN, initialCause = this)
  }
}
