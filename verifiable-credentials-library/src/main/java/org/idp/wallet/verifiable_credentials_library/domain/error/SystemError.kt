package org.idp.wallet.verifiable_credentials_library.domain.error

enum class SystemError(val error: String, val code: String, val description: String) {
  UNEXPECTED("system_error", "9000", "unexpected error, please try again later."),
}
