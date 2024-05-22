package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

class CredentialOfferRequestValidator(private val request: CredentialOfferRequest) {

  fun validate() {
    throwExceptionIfNotValidScheme()
  }

  private fun throwExceptionIfNotValidScheme() {
    val scheme = request.scheme
    if (scheme == null) {
      throw RuntimeException("")
    }
    if (scheme != "openid-credential-offer://") {
      throw RuntimeException("")
    }
  }
}
