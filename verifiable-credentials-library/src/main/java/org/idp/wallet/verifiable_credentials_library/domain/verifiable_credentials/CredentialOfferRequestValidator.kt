package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

class CredentialOfferRequestValidator(private val request: CredentialOfferRequest) {

  private val scheme = request.scheme
  private val params = request.params

  fun validate() {
    throwExceptionIfNotValidScheme()
    throwExceptionIfRequiredParams()
    throwExceptionIfDuplicatedParams()
  }

  private fun throwExceptionIfNotValidScheme() {
    if (scheme == null) {
      throw CredentialOfferRequestException("Scheme is required.")
    }
    if (scheme != "openid-credential-offer") {
      throw CredentialOfferRequestException("Scheme must be 'openid-credential-offer://'.")
    }
  }

  private fun throwExceptionIfRequiredParams() {
    if (!params.containsKey("credential_offer") && !params.containsKey("credential_offer_uri")) {
      throw CredentialOfferRequestException(
          "Credential offer request must contain either credential_offer or credential_offer_uri.")
    }
  }

  private fun throwExceptionIfDuplicatedParams() {
    if (params.containsKey("credential_offer") && params.containsKey("credential_offer_uri")) {
      throw CredentialOfferRequestException(
          "Credential offer request must not contain both credential_offer and credential_offer_uri.")
    }
  }
}
