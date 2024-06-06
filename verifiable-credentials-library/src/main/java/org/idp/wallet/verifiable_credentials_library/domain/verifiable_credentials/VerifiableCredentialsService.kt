package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import id.walt.sdjwt.SDJwt
import java.util.UUID
import kotlin.js.ExperimentalJsExport
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialResponse
import org.json.JSONObject

class VerifiableCredentialsService(
    val registry: VerifiableCredentialRegistry,
    val clientId: String
) {

  @OptIn(ExperimentalJsExport::class)
  fun transform(format: String, rawVc: String): VerifiableCredentialsRecord {
    return when (format) {
      "vc+sd-jwt" -> {
        val sdJwt = SDJwt.parse(rawVc)
        val fullPayload = sdJwt.fullPayload
        VerifiableCredentialsRecord(UUID.randomUUID().toString(), format, rawVc, fullPayload)
      }
      "jwt_vc_json" -> {
        val jwt = JoseHandler.parse(rawVc)
        val payload = jwt.payload()
        VerifiableCredentialsRecord(UUID.randomUUID().toString(), format, rawVc, payload)
      }
      else -> {
        throw RuntimeException("unsupported format")
      }
    }
  }

  fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return registry.getAll()
  }

  suspend fun getCredentialOffer(credentialOfferRequest: CredentialOfferRequest): org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer {
    val credentialOfferUri = credentialOfferRequest.credentialOfferUri()
    credentialOfferUri?.let {
      val response = HttpClient.get(it)
      return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferCreator.create(response)
    }
    val credentialOffer = credentialOfferRequest.credentialOffer()
    credentialOffer?.let {
      val json = JSONObject(it)
      return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferCreator.create(json)
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

  suspend fun requestTokenOnPreAuthorizedCode(
      url: String,
      preAuthorizationCode: String
  ): TokenResponse {
    val tokenRequest =
        hashMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code"),
            Pair("pre-authorized_code", preAuthorizationCode))
    val tokenRequestHeaders = hashMapOf(Pair("content-type", "application/x-www-form-urlencoded"))
    val response = HttpClient.post(url, headers = tokenRequestHeaders, requestBody = tokenRequest)
    return JsonUtils.read(response.toString(), TokenResponse::class.java)
  }

  suspend fun requestCredential(
      url: String,
      accessToken: String,
      format: String,
      vc: String
  ): CredentialResponse {
    val credentialRequest = hashMapOf(Pair("format", format), Pair("vct", vc))
    val credentialRequestHeader = hashMapOf(Pair("Authorization", "Bearer $accessToken"))
    val response = HttpClient.post(url, credentialRequestHeader, credentialRequest)
    return JsonUtils.read(response.toString(), CredentialResponse::class.java)
  }

  fun registerCredential(
      credentialIssuer: String,
      verifiableCredentialsRecord: VerifiableCredentialsRecord
  ) {
    registry.save(credentialIssuer, verifiableCredentialsRecord)
  }
}
