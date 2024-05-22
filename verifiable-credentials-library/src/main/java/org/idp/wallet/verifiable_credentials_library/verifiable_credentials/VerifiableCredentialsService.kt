package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import id.walt.sdjwt.SDJwt
import java.net.URLDecoder
import java.util.UUID
import kotlin.js.ExperimentalJsExport
import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.json.JSONObject

class VerifiableCredentialsService(
    val registry: VerifiableCredentialRegistry,
    val clientId: String
) {

  suspend fun requestVCI(url: String, format: String = "vc+sd-jwt"): JSONObject {
    val queries = extractQueries(url)

    val credentialOfferResponse = getCredentialOfferResponse(url)
    val credentialIssuer = credentialOfferResponse.getString("credential_issuer")
    val openidCredentialIssuerEndpoint = credentialIssuer + "/.well-known/openid-credential-issuer"
    val oiddEndpoint =
        credentialOfferResponse.getString("credential_issuer") + "/.well-known/openid-configuration"
    val metaResponse = HttpClient.get(oiddEndpoint)
    val tokenEndpoint = metaResponse.getString("token_endpoint")
    val preAuthorizationCode =
        credentialOfferResponse
            .getJSONObject("grants")
            .getJSONObject("urn:ietf:params:oauth:grant-type:pre-authorized_code")
            .getString("pre-authorized_code")
    val tokenRequest =
        hashMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code"),
            Pair("pre-authorized_code", preAuthorizationCode))
    val tokenRequestHeaders = hashMapOf(Pair("content-type", "application/x-www-form-urlencoded"))
    val tokenResponse =
        HttpClient.post(tokenEndpoint, headers = tokenRequestHeaders, requestBody = tokenRequest)
    val accessToken = tokenResponse.getString("access_token")
    val cNonce = tokenResponse.getString("c_nonce")
    val vcMetaResponse = HttpClient.get(openidCredentialIssuerEndpoint)
    val credentialEndpoint = vcMetaResponse.getString("credential_endpoint")
    val credentialRequest =
        hashMapOf(
            Pair("format", format),
            Pair("vct", "https://credentials.example.com/identity_credential"))
    val credentialRequestHeader = hashMapOf(Pair("Authorization", "Bearer $accessToken"))
    val credentialResponse =
        HttpClient.post(credentialEndpoint, credentialRequestHeader, credentialRequest)
    val rawVc = credentialResponse.optString("credential")
    val verifiableCredentialsRecord = transform(format, rawVc)
    registry.save(credentialIssuer, verifiableCredentialsRecord)
    return credentialResponse
  }

  @OptIn(ExperimentalJsExport::class)
  private fun transform(format: String, rawVc: String): VerifiableCredentialsRecord {
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

  private suspend fun getCredentialOfferResponse(url: String): JSONObject {
    if (url.contains("openid-credential-offer://?credential_offer_uri=")) {
      val encodedCredentialOfferUri =
          url.substring("openid-credential-offer://?credential_offer_uri=".length)
      val decodedCredentialOfferUri = URLDecoder.decode(encodedCredentialOfferUri)
      return HttpClient.get(decodedCredentialOfferUri)
    }
    val encodedCredentialOfferResponse =
        url.substring("openid-credential-offer://?credential_offer=".length)
    val decodedCredentialOfferResponse = URLDecoder.decode(encodedCredentialOfferResponse)
    return JSONObject(decodedCredentialOfferResponse)
  }
}
