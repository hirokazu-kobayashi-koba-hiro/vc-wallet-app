package org.idp.wallet.verifiable_credentials_library.domain.error

import android.util.Log

interface VerifiableCredentialsError {
  fun code(): String

  fun description(): String

  fun cause(): Throwable?
}

fun Exception.toVerifiableCredentialsError(): VerifiableCredentialsError {
  Log.e("VcWalletLibrary", this.message, this)
  return when (this) {
    is VerifiableCredentialsException ->  this
    is NetworkException -> this
    is OAuthBadRequestException -> this
    is SettingInvalidException -> this
    else ->
        VerifiableCredentialsException(VcError.UNSUPPORTED_CREDENTIAL_FORMAT, initialCause = this)
  }
}
