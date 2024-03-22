package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.net.Uri
import android.util.Log
import id.walt.sdjwt.SDJwt
import org.idp.wallet.verifiable_credentials_library.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.jose.JwtObject
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder


class VerifiableCredentialsService(context: Context, val clientId: String) {

    val registry = VerifiableCredentialRegistry(context)

    suspend fun requestVCI(url: String, format: String = "vc+sd-jwt"): JSONObject {
        val credentialOfferResponse = getCredentialOfferResponse(url)
        val credentialIssuer = credentialOfferResponse.getString("credential_issuer")
        val openidCredentialIssuerEndpoint =
            credentialIssuer + "/.well-known/openid-credential-issuer"
        val oiddEndpoint =
            credentialOfferResponse.getString("credential_issuer") + "/.well-known/openid-configuration"
        val metaResponse = HttpClient.get(oiddEndpoint)
        val tokenEndpoint = metaResponse.getString("token_endpoint")
        val preAuthorizationCode = credentialOfferResponse.getJSONObject("grants")
            .getJSONObject("urn:ietf:params:oauth:grant-type:pre-authorized_code")
            .getString("pre-authorized_code")
        val tokenRequest = hashMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code"),
            Pair("pre-authorized_code", preAuthorizationCode)
        )
        val tokenRequestHeaders = hashMapOf(
            Pair("content-type", "application/x-www-form-urlencoded")
        )
        val tokenResponse = HttpClient.post(
            tokenEndpoint,
            headers = tokenRequestHeaders,
            requestBody = tokenRequest
        )
        val accessToken = tokenResponse.getString("access_token")
        val cNonce = tokenResponse.getString("c_nonce")
        val vcMetaResponse = HttpClient.get(openidCredentialIssuerEndpoint)
        val credentialEndpoint = vcMetaResponse.getString("credential_endpoint")
        val credentialRequest = hashMapOf(
            Pair("format", format),
            Pair("vct", "https://credentials.example.com/identity_credential")
        )
        val credentialRequestHeader = hashMapOf(
            Pair("Authorization", "Bearer $accessToken")
        )
        val credentialResponse =
            HttpClient.post(credentialEndpoint, credentialRequestHeader, credentialRequest)
        registry.save(credentialIssuer, credentialResponse.optString("credential"))
        return credentialResponse
    }

    fun getAllCredentials(): JSONObject {
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
            url.substring("openid-credential-offer://?credential_offer=".length);
        val decodedCredentialOfferResponse = URLDecoder.decode(encodedCredentialOfferResponse)
        return JSONObject(decodedCredentialOfferResponse)
    }

}