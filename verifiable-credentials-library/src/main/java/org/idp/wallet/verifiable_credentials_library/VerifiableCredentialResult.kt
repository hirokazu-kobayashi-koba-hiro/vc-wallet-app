package org.idp.wallet.verifiable_credentials_library

sealed class VerifiableCredentialResult<out T, out ERROR> {

  data class Success<out T>(val data: T) : VerifiableCredentialResult<T, Nothing>()

  data class Failure<out ERROR>(val error: ERROR) : VerifiableCredentialResult<Nothing, ERROR>()
}
