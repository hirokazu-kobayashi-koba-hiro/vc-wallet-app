package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.DpopJwtCreator
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.VerifiableCredentialsAuthorizationRequest
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
      subject: String,
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
    if (!result.first) {
      throw RuntimeException("user canceled")
    }
    val tokenResponse =
        service.requestTokenWithPreAuthorizedCode(
            url = oidcMetadata.tokenEndpoint,
            preAuthorizationCode = preAuthorizedCodeGrant.preAuthorizedCode,
            txCode = result.second)
    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            tokenResponse.accessToken,
            format,
            "https://credentials.example.com/identity_credential")
    val jwtVcIssuerResponse = service.getJwksConfiguration(credentialOffer.jwtVcIssuerEndpoint())
    val jwks = service.getJwks(jwtVcIssuerResponse.jwksUri)
    credentialResponse.credential?.let {
      val verifiableCredentialsRecord =
          service.transform(
              issuer = credentialOffer.credentialIssuer,
              format = format,
              type = credentialOffer.credentialConfigurationIds[0],
              it,
              jwks)
      service.registerCredential(subject, verifiableCredentialsRecord)
    }
  }

  suspend fun handleAuthorizationCode(
      context: Context,
      subject: String,
      issuer: String,
      format: String,
  ) {
    val credentialIssuerMetadata =
        service.getCredentialIssuerMetadata("$issuer/.well-known/openid-credential-issuer")

    val oidcMetadata =
        service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())

    val authenticationRequestUri = createAuthenticationRequestUri(oidcMetadata)
    val authenticationResponse =
        OpenIdConnectApi.request(
            context = context, authenticationRequestUri = authenticationRequestUri)
    val dpopJwt =
        oidcMetadata.dpopSigningAlgValuesSupported?.let {
          return@let DpopJwtCreator.create(
              privateKey = service.getWalletPrivateKey(),
              method = "POST",
              path = oidcMetadata.tokenEndpoint)
        }
    val tokenResponse =
        service.requestTokenWithAuthorizedCode(
            url = oidcMetadata.tokenEndpoint,
            dpopJwt = dpopJwt,
            authorizationCode = authenticationResponse.code(),
        )

    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            tokenResponse.accessToken,
            format,
            "https://credentials.example.com/identity_credential")
    val jwtVcIssuerResponse = service.getJwksConfiguration("$issuer/.well-known/jwt-vc-issuer")
    val jwks = service.getJwks(jwtVcIssuerResponse.jwksUri)
    credentialResponse.credential?.let {
      val verifiableCredentialsRecord =
          service.transform(issuer = issuer, format = format, type = "", it, jwks)
      service.registerCredential(subject, verifiableCredentialsRecord)
    }
  }

  private suspend fun createAuthenticationRequestUri(oidcMetadata: OidcMetadata): String {
    oidcMetadata.pushedAuthorizationRequestEndpoint?.let { endpoint ->
      val dpopJwt =
          oidcMetadata.dpopSigningAlgValuesSupported?.let {
            return@let DpopJwtCreator.create(
                privateKey = service.getWalletPrivateKey(), method = "POST", path = endpoint)
          }

      val pushAuthenticationResponse =
          OpenIdConnectApi.pushAuthenticationRequest(
              url = endpoint,
              dpopJwt = dpopJwt,
              body =
                  mapOf(
                      "issuer" to oidcMetadata.issuer,
                      "client_id" to service.clientId,
                      "scope" to oidcMetadata.scopesSupportedAsString(),
                  ))
      val vcAuthorizationRequest =
          VerifiableCredentialsAuthorizationRequest(
              issuer = oidcMetadata.issuer,
              requestUri = pushAuthenticationResponse.requestUri,
          )
      return "${oidcMetadata.authorizationEndpoint}${vcAuthorizationRequest.queries()}"
    }

    val vcAuthorizationRequest =
        VerifiableCredentialsAuthorizationRequest(
            issuer = oidcMetadata.issuer,
            clientId = service.clientId,
            scope = oidcMetadata.scopesSupportedAsString(),
            redirectUri = "",
        )
    return "${oidcMetadata.authorizationEndpoint}${vcAuthorizationRequest.queries()}"
  }

  private suspend fun interact(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      interactor: VerifiableCredentialInteractor
  ): Pair<Boolean, String?> = suspendCoroutine { continuation ->
    val callback =
        object : VerifiableCredentialInteractorCallback {
          override fun accept(txCode: String) {
            continuation.resume(Pair(true, txCode))
          }

          override fun reject() {
            continuation.resume(Pair(false, null))
          }
        }
    interactor.confirm(context, credentialIssuerMetadata, credentialOffer, callback)
  }

  suspend fun getAllCredentials(subject: String): Map<String, VerifiableCredentialsRecords> {
    return service.getAllCredentials(subject)
  }
}
