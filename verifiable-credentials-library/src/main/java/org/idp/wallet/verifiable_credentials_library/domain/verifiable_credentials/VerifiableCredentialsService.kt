package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import android.util.Log
import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.JwtVcConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.VerifiableCredentialsType
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.json.JSONObject

class VerifiableCredentialsService(
    private val walletConfigurationService: WalletConfigurationService,
    private val verifiableCredentialRecordRepository: VerifiableCredentialRecordRepository,
    private val walletClientConfigurationRepository: WalletClientConfigurationRepository,
    private val credentialIssuanceResultRepository: CredentialIssuanceResultRepository,
) {

  suspend fun getAllCredentials(subject: String): Map<String, VerifiableCredentialsRecords> {
    return verifiableCredentialRecordRepository.getAll(subject)
  }

  suspend fun getCredentialOffer(credentialOfferRequest: CredentialOfferRequest): CredentialOffer {
    val credentialOfferUri = credentialOfferRequest.credentialOfferUri()
    credentialOfferUri?.let {
      val response = HttpClient.get(it)
      return CredentialOfferCreator.create(response)
    }
    val credentialOffer = credentialOfferRequest.credentialOffer()
    credentialOffer?.let {
      val json = JSONObject(it)
      return CredentialOfferCreator.create(json)
    }
    throw CredentialOfferRequestException(
        "Credential offer request must contain either credential_offer or credential_offer_uri.")
  }

  suspend fun getCredentialIssuerMetadata(url: String): CredentialIssuerMetadata {
    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), CredentialIssuerMetadata::class.java)
  }

  suspend fun getOidcMetadata(url: String): OidcMetadata {
    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), OidcMetadata::class.java)
  }

  suspend fun requestTokenWithPreAuthorizedCode(
      url: String,
      clientId: String,
      preAuthorizationCode: String,
      txCode: String?
  ): TokenResponse {
    val tokenRequest =
        mutableMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code"),
            Pair("pre-authorized_code", preAuthorizationCode))
    txCode?.let { tokenRequest.put("tx_code", it) }
    val tokenRequestHeaders = hashMapOf(Pair("content-type", "application/x-www-form-urlencoded"))
    val response = HttpClient.post(url, headers = tokenRequestHeaders, requestBody = tokenRequest)
    return JsonUtils.read(response.toString(), TokenResponse::class.java)
  }

  suspend fun requestTokenWithAuthorizedCode(
      url: String,
      clientId: String,
      dpopJwt: String? = null,
      authorizationCode: String,
      redirectUri: String? = null,
  ): TokenResponse {
    val tokenRequest =
        mutableMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "authorization_code"),
            Pair("code", authorizationCode))
    redirectUri?.let { tokenRequest.put("redirect_uri", it) }
    val tokenRequestHeaders =
        mutableMapOf(Pair("content-type", "application/x-www-form-urlencoded"))
    dpopJwt?.let { tokenRequestHeaders.put("DPoP", it) }
    val response = HttpClient.post(url, headers = tokenRequestHeaders, requestBody = tokenRequest)
    return JsonUtils.read(response.toString(), TokenResponse::class.java)
  }

  suspend fun requestCredential(
      url: String,
      dpopJwt: String?,
      accessToken: String,
      verifiableCredentialType: VerifiableCredentialsType,
      vct: String?
  ): CredentialResponse {
    val credentialRequest =
        mutableMapOf(
            Pair("format", verifiableCredentialType.format),
            Pair("doctype", verifiableCredentialType.doctype))
    vct?.let { credentialRequest.put("vct", it) }
    val credentialRequestHeader =
        dpopJwt?.let {
          return@let mutableMapOf("Authorization" to "DPoP $accessToken", "DPoP" to it)
        } ?: mutableMapOf(Pair("Authorization", "Bearer $accessToken"))
    credentialRequestHeader.put("content-type", "application/json")
    val response = HttpClient.post(url, credentialRequestHeader, credentialRequest)
    return JsonUtils.read(response.toString(), CredentialResponse::class.java)
  }

  suspend fun requestDeferredCredential(
      url: String,
      dpopJwt: String?,
      accessToken: String,
      transactionId: String
  ): CredentialResponse {
    val credentialRequest = mutableMapOf(Pair("transaction_id", transactionId))
    val credentialRequestHeader =
        dpopJwt?.let {
          return@let mutableMapOf("Authorization" to "DPoP $accessToken", "DPoP" to it)
        } ?: mutableMapOf(Pair("Authorization", "Bearer $accessToken"))
    credentialRequestHeader.put("content-type", "application/json")
    val response = HttpClient.post(url, credentialRequestHeader, credentialRequest)
    return JsonUtils.read(response.toString(), CredentialResponse::class.java)
  }

  suspend fun registerCredential(
      subject: String,
      verifiableCredentialsRecord: VerifiableCredentialsRecord
  ) {
    verifiableCredentialRecordRepository.save(subject, verifiableCredentialsRecord)
  }

  suspend fun registerCredentialIssuanceResult(
      subject: String,
      issuer: String,
      credentialConfigurationId: String,
      credentialResponse: CredentialResponse,
  ) {
    val id = UUID.randomUUID().toString()
    val credentialIssuanceResult =
        CredentialIssuanceResult(
            id = id,
            issuer = issuer,
            credentialConfigurationId = credentialConfigurationId,
            credential = credentialResponse.credential,
            transactionId = credentialResponse.transactionId,
            cNonce = credentialResponse.cNonce,
            cNonceExpiresIn = credentialResponse.cNonceExpiresIn,
            notificationId = credentialResponse.notificationId,
            status =
                credentialResponse.credential?.let { CredentialIssuanceResultStatus.SUCCESS }
                    ?: CredentialIssuanceResultStatus.PENDING)
    credentialIssuanceResultRepository.register(
        subject = subject, credentialIssuanceResult = credentialIssuanceResult)
  }

  suspend fun findAllCredentialIssuanceResults(subject: String): List<CredentialIssuanceResult> {
    return credentialIssuanceResultRepository.findAll(subject)
  }

  suspend fun getCredentialIssuanceResult(subject: String, id: String): CredentialIssuanceResult {
    return credentialIssuanceResultRepository.get(subject, id)
  }

  suspend fun updateCredentialIssuanceResult(
      subject: String,
      credentialIssuanceResult: CredentialIssuanceResult
  ) {
    credentialIssuanceResultRepository.update(
        subject = subject, credentialIssuanceResult = credentialIssuanceResult)
  }

  suspend fun getJwks(jwksEndpoint: String): String {
    val response = HttpClient.get(jwksEndpoint)
    return response.toString()
  }

  suspend fun getJwksConfiguration(jwtVcIssuerEndpoint: String): JwtVcConfiguration {
    val response = HttpClient.get(jwtVcIssuerEndpoint)
    return JsonUtils.read(response.toString(), JwtVcConfiguration::class.java)
  }

  fun getWalletPrivateKey(): String {
    return walletConfigurationService.getWalletPrivateKey()
  }

  suspend fun getClientConfiguration(oidcMetadata: OidcMetadata): ClientConfiguration {
    val walletClientConfiguration = walletClientConfigurationRepository.find(oidcMetadata.issuer)
    walletClientConfiguration?.let {
      return it
    }
    val clientConfiguration = registerClientConfiguration(oidcMetadata)
    walletClientConfigurationRepository.register(oidcMetadata.issuer, clientConfiguration)
    return clientConfiguration
  }

  suspend fun registerClientConfiguration(oidcMetadata: OidcMetadata): ClientConfiguration {
    try {
      // FIXME dynamic configuration
      val redirectUris =
          listOf(
              "org.idp.verifiable.credentials://dev-l6ns7qgdx81yv2rs.us.auth0.com/android/org.idp.wallet.app/callback")
      val grantTypes = oidcMetadata.grantTypesSupported ?: listOf()
      val responseTypes: List<String> = ArrayList()
      val clientName = "verifiable_credentials_library"
      val scope: String = oidcMetadata.scopesSupportedAsString()
      val requestBody =
          mapOf(
              "redirect_uris" to redirectUris,
              "grant_types" to grantTypes,
              "response_types" to responseTypes,
              "client_Uri" to clientName,
              "scope" to scope,
              "application_type" to "native",
              "token_endpoint_auth_method" to "none",
          )
      val registrationEndpoint =
          oidcMetadata.registrationEndpoint
              ?: throw RuntimeException(
                  String.format("not configure registration endpoint (%s)", oidcMetadata.issuer))

      val response = HttpClient.post(registrationEndpoint, requestBody = requestBody)
      return ClientConfiguration(
          clientId = response.getString("client_id"),
          clientSecret = response.optString("client_secret", ""),
          redirectUris = redirectUris,
          grantTypes = grantTypes,
          responseTypes = responseTypes,
          clientUri = clientName,
          scope = scope)
    } catch (e: Exception) {
      // FIXME
      Log.e("VcWalletLibrary", e.message ?: "registerClientConfiguration failed")
      return ClientConfiguration(
          clientId = "218232426",
      )
    }
  }
}
