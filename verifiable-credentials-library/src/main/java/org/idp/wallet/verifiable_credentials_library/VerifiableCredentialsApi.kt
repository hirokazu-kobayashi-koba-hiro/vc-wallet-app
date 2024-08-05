package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.DpopJwtCreator
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.VerifiableCredentialsAuthorizationRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResult
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequestValidator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService

object VerifiableCredentialsApi {

  lateinit var service: VerifiableCredentialsService

  fun initialize(service: VerifiableCredentialsService) {
    this.service = service
  }

  suspend fun handlePreAuthorization(
      context: Context,
      subject: String,
      url: String,
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
    val clientConfiguration = service.getClientConfiguration(oidcMetadata)
    val result = interact(context, credentialIssuerMetadata, credentialOffer, interactor)
    if (!result.first) {
      throw RuntimeException("user canceled")
    }
    val tokenResponse =
        service.requestTokenWithPreAuthorizedCode(
            url = oidcMetadata.tokenEndpoint,
            clientId = clientConfiguration.clientId,
            preAuthorizationCode = preAuthorizedCodeGrant.preAuthorizedCode,
            txCode = result.second)
    val verifiableCredentialsType =
        credentialIssuerMetadata.getVerifiableCredentialsType(
            credentialOffer.credentialConfigurationIds[0])
    val vct = credentialIssuerMetadata.findVct(credentialOffer.credentialConfigurationIds[0])
    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            null,
            tokenResponse.accessToken,
            verifiableCredentialsType,
            vct)
    val jwtVcIssuerResponse = service.getJwksConfiguration(credentialOffer.jwtVcIssuerEndpoint())
    val jwks = service.getJwks(jwtVcIssuerResponse.jwksUri)
    credentialResponse.credential?.let {
      val verifiableCredentialsRecord =
          service.transform(
              issuer = credentialOffer.credentialIssuer,
              verifiableCredentialsType = verifiableCredentialsType,
              type = credentialOffer.credentialConfigurationIds[0],
              it,
              jwks)
      service.registerCredential(subject, verifiableCredentialsRecord)
    }
    service.registerCredentialIssuanceResult(
        subject = subject, issuer = credentialOffer.credentialIssuer, credentialResponse)
  }

  suspend fun handleAuthorizationCode(
      context: Context,
      subject: String,
      issuer: String,
      credentialConfigurationId: String,
  ) {
    val credentialIssuerMetadata =
        service.getCredentialIssuerMetadata("$issuer/.well-known/openid-credential-issuer")

    val oidcMetadata =
        service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())

    val clientConfiguration = service.getClientConfiguration(oidcMetadata)
    val authenticationRequestUri =
        createAuthenticationRequestUri(
            credentialConfigurationId, credentialIssuerMetadata, oidcMetadata, clientConfiguration)
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
            clientId = clientConfiguration.clientId,
            dpopJwt = dpopJwt,
            authorizationCode = authenticationResponse.code(),
        )

    val dpopJwtForCredential =
        oidcMetadata.dpopSigningAlgValuesSupported?.let {
          return@let DpopJwtCreator.create(
              privateKey = service.getWalletPrivateKey(),
              method = "POST",
              path = credentialIssuerMetadata.credentialEndpoint,
              accessToken = tokenResponse.accessToken)
        }
    val verifiableCredentialsType =
        credentialIssuerMetadata.getVerifiableCredentialsType(credentialConfigurationId)
    val vct = credentialIssuerMetadata.findVct(credentialConfigurationId)
    val credentialResponse =
        service.requestCredential(
            credentialIssuerMetadata.credentialEndpoint,
            dpopJwtForCredential,
            tokenResponse.accessToken,
            verifiableCredentialsType,
            vct)
    val jwtVcIssuerResponse = service.getJwksConfiguration("$issuer/.well-known/jwt-vc-issuer")
    val jwks = service.getJwks(jwtVcIssuerResponse.jwksUri)
    credentialResponse.credential?.let {
      val verifiableCredentialsRecord =
          service.transform(
              issuer = issuer,
              verifiableCredentialsType = verifiableCredentialsType,
              type = credentialConfigurationId,
              it,
              jwks)
      service.registerCredential(subject, verifiableCredentialsRecord)
    }
    service.registerCredentialIssuanceResult(subject = subject, issuer = issuer, credentialResponse)
  }

  private suspend fun createAuthenticationRequestUri(
      credentialConfigurationId: String,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      oidcMetadata: OidcMetadata,
      clientConfiguration: ClientConfiguration
  ): String {
    val scope = credentialIssuerMetadata.getScope(credentialConfigurationId)
    oidcMetadata.pushedAuthorizationRequestEndpoint?.let { endpoint ->
      val dpopJwt =
          oidcMetadata.dpopSigningAlgValuesSupported?.let {
            DpopJwtCreator.create(
                privateKey = service.getWalletPrivateKey(), method = "POST", path = endpoint)
          }

      val pushAuthenticationResponse =
          OpenIdConnectApi.pushAuthenticationRequest(
              url = endpoint,
              dpopJwt = dpopJwt,
              body =
                  mapOf(
                      "issuer" to oidcMetadata.issuer,
                      "client_id" to clientConfiguration.clientId,
                      "scope" to scope,
                      "response_type" to "code",
                  ))
      val vcAuthorizationRequest =
          VerifiableCredentialsAuthorizationRequest(
              issuer = oidcMetadata.issuer,
              clientId = clientConfiguration.clientId,
              requestUri = pushAuthenticationResponse.requestUri,
          )
      return "${oidcMetadata.authorizationEndpoint}${vcAuthorizationRequest.queries()}"
    }

    val vcAuthorizationRequest =
        VerifiableCredentialsAuthorizationRequest(
            issuer = oidcMetadata.issuer,
            clientId = clientConfiguration.clientId,
            scope = scope,
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

  suspend fun findAllCredentialIssuanceResults(subject: String): List<CredentialIssuanceResult> {
    return service.findAllCredentialIssuanceResults(subject)
  }
}
