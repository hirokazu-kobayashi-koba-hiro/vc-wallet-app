package org.idp.wallet.verifiable_credentials_library.domain.error

interface VerifiableCredentialsError {
  fun code(): String

  fun description(): String

  fun cause(): Throwable?
}

fun Exception.toVerifiableCredentialsError(): VerifiableCredentialsError {
  return when (this) {
    is VerifiableCredentialsException -> this
    is NetworkException -> this
    is OAuthBadRequestException -> this
    is SettingInvalidException -> this
    else -> VerifiableCredentialsException(VcError.UNSUPPORTED_CREDENTIAL_FORMAT, cause = this)
  }
}
