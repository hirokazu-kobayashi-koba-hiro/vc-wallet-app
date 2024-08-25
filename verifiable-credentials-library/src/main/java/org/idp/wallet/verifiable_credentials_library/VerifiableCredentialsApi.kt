package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.error.VcError
import org.idp.wallet.verifiable_credentials_library.domain.error.VerifiableCredentialsError
import org.idp.wallet.verifiable_credentials_library.domain.error.VerifiableCredentialsException
import org.idp.wallet.verifiable_credentials_library.domain.error.toVerifiableCredentialsError
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.DpopJwtCreator
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.VerifiableCredentialsAuthorizationRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResult
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResultStatus
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferRequestValidator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialRequestProofCreator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialTransformer
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
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      val credentialOfferRequest = CredentialOfferRequest(url)
      val credentialOfferRequestValidator = CredentialOfferRequestValidator(credentialOfferRequest)
      credentialOfferRequestValidator.validate()
      val credentialOffer = service.getCredentialOffer(credentialOfferRequest)

      val preAuthorizedCodeGrant = credentialOffer.preAuthorizedCodeGrant
      if (preAuthorizedCodeGrant == null) {
        throw VerifiableCredentialsException(
            VcError.NOT_FOUND_REQUIRED_PARAMS,
            "PreAuthorizedCode in credential offer response is empty. It is required on pre-authorization-code flow")
      }
      val credentialIssuerMetadata =
          service.getCredentialIssuerMetadata(credentialOffer.credentialIssuerMetadataEndpoint())
      val oidcMetadata =
          service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())
      val clientConfiguration = service.getOrRegisterClientConfiguration(oidcMetadata)
      val result = interact(context, credentialIssuerMetadata, credentialOffer, interactor)
      if (!result.first) {
        throw VerifiableCredentialsException(VcError.NOT_AUTHENTICATED, "user canceled")
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
      val proof =
          CredentialRequestProofCreator(
                  cNonce = tokenResponse.cNonce,
                  clientId = clientConfiguration.clientId,
                  issuer = credentialOffer.credentialIssuer,
                  privateKey = service.getWalletPrivateKey())
              .create()
      val credentialResponse =
          service.requestCredential(
              credentialIssuerMetadata.credentialEndpoint,
              null,
              tokenResponse.accessToken,
              verifiableCredentialsType,
              vct,
              proof)
      val jwtVcIssuerResponse = service.getJwksConfiguration(credentialOffer.jwtVcIssuerEndpoint())
      val jwks = service.getJwks(jwtVcIssuerResponse.jwksUri)
      credentialResponse.credential?.let {
        val verifiableCredentialsRecord =
            VerifiableCredentialTransformer(
                    issuer = credentialOffer.credentialIssuer,
                    verifiableCredentialsType = verifiableCredentialsType,
                    type = credentialOffer.credentialConfigurationIds[0],
                    it,
                    jwks)
                .transform()
        service.registerCredential(subject, verifiableCredentialsRecord)
      }
      service.registerCredentialIssuanceResult(
          subject = subject,
          issuer = credentialOffer.credentialIssuer,
          credentialConfigurationId = credentialOffer.credentialConfigurationIds[0],
          credentialResponse = credentialResponse,
      )
      return VerifiableCredentialResult.Success(Unit)
    } catch (e: Exception) {
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  suspend fun handleAuthorizationCode(
      context: Context,
      subject: String,
      issuer: String,
      credentialConfigurationId: String,
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      val credentialIssuerMetadata =
          service.getCredentialIssuerMetadata("$issuer/.well-known/openid-credential-issuer")

      val oidcMetadata =
          service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())

      val clientConfiguration = service.getOrRegisterClientConfiguration(oidcMetadata)
      val authenticationRequestUri =
          createAuthenticationRequestUri(
              credentialConfigurationId,
              credentialIssuerMetadata,
              oidcMetadata,
              clientConfiguration)
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
            VerifiableCredentialTransformer(
                    issuer = issuer,
                    verifiableCredentialsType = verifiableCredentialsType,
                    type = credentialConfigurationId,
                    it,
                    jwks)
                .transform()
        service.registerCredential(subject, verifiableCredentialsRecord)
      }
      service.registerCredentialIssuanceResult(
          subject = subject,
          issuer = issuer,
          credentialConfigurationId = credentialConfigurationId,
          credentialResponse = credentialResponse)
      return VerifiableCredentialResult.Success(Unit)
    } catch (e: Exception) {
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  suspend fun handleDeferredCredential(
      context: Context,
      subject: String,
      credentialIssuanceResultId: String
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      val credentialIssuanceResult =
          service.getCredentialIssuanceResult(subject, credentialIssuanceResultId)
      val transactionId =
          credentialIssuanceResult.transactionId
              ?: throw VerifiableCredentialsException(
                  VcError.NOT_FOUND_REQUIRED_PARAMS, "not found transactionId")
      val credentialIssuerMetadata =
          service.getCredentialIssuerMetadata(credentialIssuanceResult.issuer)
      val deferredCredentialEndpoint =
          credentialIssuerMetadata.deferredCredentialEndpoint
              ?: throw VerifiableCredentialsException(
                  VcError.UNSUPPORTED_DEFERRED_CREDENTIAL,
                  String.format(
                      "unsupported deferredCredential (%s)", credentialIssuanceResult.issuer))

      val oidcMetadata =
          service.getOidcMetadata(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())
      val clientConfiguration = service.getOrRegisterClientConfiguration(oidcMetadata)
      val authenticationRequestUri =
          createAuthenticationRequestUri(
              credentialIssuanceResult.credentialConfigurationId,
              credentialIssuerMetadata,
              oidcMetadata,
              clientConfiguration)
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
      val deferredCredentialResponse =
          service.requestDeferredCredential(
              url = deferredCredentialEndpoint,
              dpopJwt = dpopJwtForCredential,
              accessToken = tokenResponse.accessToken,
              transactionId = transactionId)
      val verifiableCredentialsType =
          credentialIssuerMetadata.getVerifiableCredentialsType(
              credentialIssuanceResult.credentialConfigurationId)
      val jwks = service.getJwks(credentialIssuerMetadata.credentialEndpoint)
      deferredCredentialResponse.credential?.let {
        val verifiableCredentialsRecord =
            VerifiableCredentialTransformer(
                    issuer = credentialIssuanceResult.issuer,
                    verifiableCredentialsType = verifiableCredentialsType,
                    type = credentialIssuanceResult.credentialConfigurationId,
                    it,
                    jwks)
                .transform()
        service.registerCredential(subject, verifiableCredentialsRecord)
        val updatedCredentialIssuanceResult =
            credentialIssuanceResult.copy(status = CredentialIssuanceResultStatus.SUCCESS)
        service.updateCredentialIssuanceResult(
            subject = subject, credentialIssuanceResult = updatedCredentialIssuanceResult)
      }
      return VerifiableCredentialResult.Success(Unit)
    } catch (e: Exception) {
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
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

  suspend fun findCredentials(
      subject: String
  ): VerifiableCredentialResult<
      Map<String, VerifiableCredentialsRecords>, VerifiableCredentialsError> {
    try {
      val allCredentials = service.findCredentials(subject)
      return VerifiableCredentialResult.Success(allCredentials)
    } catch (e: Exception) {
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  suspend fun findCredentialIssuanceResults(
      subject: String
  ): VerifiableCredentialResult<List<CredentialIssuanceResult>, VerifiableCredentialsError> {
    try {
      val credentialIssuanceResults = service.findCredentialIssuanceResults(subject)
      return VerifiableCredentialResult.Success(credentialIssuanceResults)
    } catch (e: Exception) {
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }
}
