package org.idp.wallet.verifiable_credentials_library

import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.CredentialOfferRequest
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.CredentialOfferRequestValidator
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsService

class VerifiableCredentialsHandler(private val service: VerifiableCredentialsService) {

  suspend fun handlePreAuthorization(url: String, format: String = "vc+sd-jwt") {
    val credentialOfferRequest = CredentialOfferRequest(url)
    val credentialOfferRequestValidator = CredentialOfferRequestValidator(credentialOfferRequest)
    credentialOfferRequestValidator.validate()
    val credentialOffer = service.getCredentialOffer(credentialOfferRequest)

    val preAuthorizedCodeGrant = credentialOffer.preAuthorizedCodeGrant
    if (preAuthorizedCodeGrant == null) {
      throw RuntimeException(
          "PreAuthorizedCode in credential offer response is empty. It is required on pre-authorization-code flow")
    }
    val credentialIssuerMetadata =
        service.getCredentialIssuerMetadata(credentialOffer.credentialIssuerMetadataEndpoint())
    val oidcMetadata =
        service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())

    val tokenResponse =
        service.requestTokenOnPreAuthorizedCode(
            oidcMetadata.tokenEndpoint, preAuthorizedCodeGrant.preAuthorizedCode)
    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            tokenResponse.accessToken,
            format,
            "https://credentials.example.com/identity_credential")
    val verifiableCredentialsRecord = service.transform(format, credentialResponse.credential)
    //        registry.save(credentialIssuer, verifiableCredentialsRecord)

  }

  fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return service.getAllCredentials()
  }
}
