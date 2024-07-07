package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequestValidator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService

class VerifiableCredentialsApi(private val service: VerifiableCredentialsService) {

  suspend fun handlePreAuthorization(
      context: Context,
      url: String,
      format: String = "vc+sd-jwt",
      interactor: VerifiableCredentialInteractor
  ) {
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
    val result = interact(context, credentialIssuerMetadata, credentialOffer, interactor)
    if (!result) {
      throw RuntimeException("")
    }
    val tokenResponse =
        service.requestTokenOnPreAuthorizedCode(
            oidcMetadata.tokenEndpoint, preAuthorizedCodeGrant.preAuthorizedCode)
    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            tokenResponse.accessToken,
            format,
            "https://credentials.example.com/identity_credential")
    credentialResponse.credential?.let {
      val verifiableCredentialsRecord =
          service.transform(
              format = format, type = credentialOffer.credentialConfigurationIds[0], it)
      service.registerCredential(credentialOffer.credentialIssuer, verifiableCredentialsRecord)
    }
  }

  private suspend fun interact(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      interactor: VerifiableCredentialInteractor
  ): Boolean = suspendCoroutine { continuation ->
    val callback =
        object : VerifiableCredentialInteractorCallback {
          override fun accept(pinCode: String) {
            continuation.resume(true)
          }

          override fun reject() {
            continuation.resume(false)
          }
        }
    interactor.confirm(context, credentialIssuerMetadata, credentialOffer, callback)
  }

  fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return service.getAllCredentials()
  }
}
