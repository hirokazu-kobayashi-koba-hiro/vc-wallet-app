package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.JwtVcConfiguration
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.util.sdjwt.SdJwtUtils
import org.json.JSONObject

class VerifiableCredentialsService(
    val walletConfigurationService: WalletConfigurationService,
    val repository: VerifiableCredentialRecordRepository,
    val clientId: String
) {

  suspend fun transform(
      issuer: String,
      format: String,
      type: String,
      rawVc: String,
      jwks: String
  ): VerifiableCredentialsRecord {
    return when (format) {
      "vc+sd-jwt" -> {
        val claims = SdJwtUtils.parseAndVerifySignature(rawVc, jwks)
        return VerifiableCredentialsRecord(
            UUID.randomUUID().toString(), issuer, type, format, rawVc, claims)
      }
      "jwt_vc_json" -> {
        val jwt = JoseUtils.parseAndVerifySignature(rawVc, jwks)
        val payload = jwt.payload()
        VerifiableCredentialsRecord(
            UUID.randomUUID().toString(), issuer, type, format, rawVc, payload)
      }
      "mso_mdoc" -> {
        VerifiableCredentialsRecord(
            UUID.randomUUID().toString(), issuer, type, format, rawVc, mapOf())
      }
      else -> {
        throw RuntimeException("unsupported format")
      }
    }
  }

  suspend fun getAllCredentials(subject: String): Map<String, VerifiableCredentialsRecords> {
    return repository.getAll(subject)
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
      format: String,
      vc: String
  ): CredentialResponse {
    val credentialRequest = mapOf(Pair("format", format), Pair("vct", vc))
    val credentialRequestHeader =
        dpopJwt?.let {
          return@let mapOf("Authorization" to "DPoP $accessToken", "DPoP" to it)
        } ?: mapOf(Pair("Authorization", "Bearer $accessToken"))
    val response = HttpClient.post(url, credentialRequestHeader, credentialRequest)
    return JsonUtils.read(response.toString(), CredentialResponse::class.java)
  }

  suspend fun registerCredential(
      subject: String,
      verifiableCredentialsRecord: VerifiableCredentialsRecord
  ) {
    repository.save(subject, verifiableCredentialsRecord)
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
}
