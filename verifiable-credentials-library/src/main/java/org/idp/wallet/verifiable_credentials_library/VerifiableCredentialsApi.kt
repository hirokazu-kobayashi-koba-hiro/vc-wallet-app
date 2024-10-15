package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
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

/**
 * This singleton object provides an API to interact with verifiable credentials. It offers
 * functions to handle pre-authorization, authorization code flow, deferred credentials, and to find
 * credentials and credential issuance results.
 */
object VerifiableCredentialsApi {

  private lateinit var configuration: WalletConfiguration
  private lateinit var service: VerifiableCredentialsService

  /**
   * Initializes the VerifiableCredentialsApi with the necessary configuration and service.
   *
   * @param configuration the wallet configuration containing necessary keys and settings
   * @param service the verifiable credentials service instance
   */
  internal fun initialize(
      configuration: WalletConfiguration,
      service: VerifiableCredentialsService
  ) {
    this.configuration = configuration
    this.service = service
  }

  /**
   * Handles the pre-authorization flow for verifiable credentials.
   *
   * @param context the context of the application
   * @param subject the subject identifier for whom the credentials are issued
   * @param url the URL to be used in the pre-authorization flow
   * @param interactor the interactor to handle user interaction during the flow
   * @return a result indicating success or failure of the operation
   */
  suspend fun handlePreAuthorization(
      context: Context,
      subject: String,
      url: String,
      interactor: VerifiableCredentialInteractor
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      verifyInitialized()

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
                  privateKey = configuration.privateKey)
              .create()
      val credentialResponse =
          service.requestCredential(
              credentialIssuerMetadata.credentialEndpoint,
              null,
              tokenResponse.accessToken,
              verifiableCredentialsType,
              vct,
              proof)

      val jwtVcIssuerMetadata = service.getJwksConfiguration(credentialOffer.jwtVcIssuerEndpoint())
      val jwks = service.getJwks(jwtVcIssuerMetadata)
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

  /**
   * Handles the authorization code flow for obtaining verifiable credentials.
   *
   * @param context the context of the application
   * @param subject the subject identifier for whom the credentials are issued
   * @param issuer the issuer of the credentials
   * @param credentialConfigurationId the identifier of the credential configuration
   * @return a result indicating success or failure of the operation
   */
  suspend fun handleAuthorizationCode(
      context: Context,
      subject: String,
      issuer: String,
      credentialConfigurationId: String,
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      verifyInitialized()

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
                privateKey = configuration.privateKey,
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
                privateKey = configuration.privateKey,
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

      val jwtVcIssuerMetadata = service.getJwksConfiguration("$issuer/.well-known/jwt-vc-issuer")
      val jwks = service.getJwks(jwtVcIssuerMetadata)
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

  /**
   * Handles the deferred credential issuance flow.
   *
   * @param context the context of the application
   * @param subject the subject identifier for whom the credentials are issued
   * @param credentialIssuanceResultId the identifier of the credential issuance result
   * @return a result indicating success or failure of the operation
   */
  suspend fun handleDeferredCredential(
      context: Context,
      subject: String,
      credentialIssuanceResultId: String
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      verifyInitialized()

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
                privateKey = configuration.privateKey,
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
                privateKey = configuration.privateKey,
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
      val jwtVcIssuerMetadata =
          service.getJwksConfiguration(credentialIssuerMetadata.getOpenIdConfigurationEndpoint())
      val jwks = service.getJwks(jwtVcIssuerMetadata)
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

  /**
   * Creates an authentication request URI for the verifiable credentials authorization request.
   *
   * @param credentialConfigurationId the identifier of the credential configuration
   * @param credentialIssuerMetadata the metadata of the credential issuer
   * @param oidcMetadata the OIDC metadata
   * @param clientConfiguration the client configuration for the request
   * @return the URI for the authentication request
   */
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
                privateKey = configuration.privateKey, method = "POST", path = endpoint)
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

  /**
   * Interacts with the user to confirm or reject the credential offer.
   *
   * @param context the context of the application
   * @param credentialIssuerMetadata the metadata of the credential issuer
   * @param credentialOffer the offer details for the credential
   * @param interactor the interactor handling the user interaction
   * @return a pair containing a boolean indicating acceptance or rejection, and a transaction code
   *   if accepted
   */
  private suspend fun interact(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      interactor: VerifiableCredentialInteractor
  ): Pair<Boolean, String?> = suspendCoroutine { continuation ->
    val callback =
        object : VerifiableCredentialInteractorCallback {
          override fun accept(txCode: String?) {
            continuation.resume(Pair(true, txCode))
          }

          override fun reject() {
            continuation.resume(Pair(false, null))
          }
        }
    interactor.confirm(context, credentialIssuerMetadata, credentialOffer, callback)
  }

  /**
   * Finds all verifiable credentials associated with the given subject.
   *
   * @param subject the subject identifier to search credentials for
   * @return a result containing a lis of credentials or an error if the operation fails
   */
  suspend fun findCredentials(
      subject: String
  ): VerifiableCredentialResult<VerifiableCredentialsRecords, VerifiableCredentialsError> {
    try {
      verifyInitialized()

      val allCredentials = service.findCredentials(subject)
      return VerifiableCredentialResult.Success(allCredentials)
    } catch (e: Exception) {

      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  /**
   * Finds all credential issuance results associated with the given subject.
   *
   * @param subject the subject identifier to search credential issuance results for
   * @return a result containing a list of credential issuance results or an error if the operation
   *   fails
   */
  suspend fun findCredentialIssuanceResults(
      subject: String
  ): VerifiableCredentialResult<List<CredentialIssuanceResult>, VerifiableCredentialsError> {
    try {
      verifyInitialized()

      val credentialIssuanceResults = service.findCredentialIssuanceResults(subject)
      return VerifiableCredentialResult.Success(credentialIssuanceResults)
    } catch (e: Exception) {

      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  /**
   * Verifies if the API has been properly initialized with configuration and service. Throws an
   * exception if not initialized.
   */
  private fun verifyInitialized() {
    if (!this::configuration.isInitialized || !this::service.isInitialized) {
      throw VerifiableCredentialsException(
          VcError.NOT_INITIALIZED,
          "not initialized, please call VerifiableCredentialsClient.initialize")
    }
  }
}
