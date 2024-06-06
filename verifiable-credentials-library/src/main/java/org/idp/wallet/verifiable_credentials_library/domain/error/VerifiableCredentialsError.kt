package org.idp.wallet.verifiable_credentials_library.domain.error

interface VerifiableCredentialsError {
  fun code(): String

  fun description(): String

  fun cause(): Throwable?
}
