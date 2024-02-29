package org.idp.wallet.verifiable_credentials_library

import org.idp.wallet.verifiable_credentials_library.http.HttpClient
import org.json.JSONObject
import java.net.URLDecoder
import java.util.Base64

class VerifiableCredentialsClient(val clientId: String) {

    suspend fun requestVCI(url: String): JSONObject {
        val credentialOfferResponse = getCredentialOfferResponse(url)
        //{"credential_issuer":"https://trial.authlete.net","credential_configuration_ids":["IdentityCredential","org.iso.18013.5.1.mDL"],"grants":{"urn:ietf:params:oauth:grant-type:pre-authorized_code":{"pre-authorized_code":"9EzVDw3GeT5qF0_1m2zaBAEWuPQnuVSJ_wZfw_D2CDY"}}}
        val openidCredentialIssuerEndpoint =
            credentialOfferResponse.getString("credential_issuer") + "/.well-known/openid-credential-issuer"
        val oiddEndpoint = credentialOfferResponse.getString("credential_issuer") + "/.well-known/openid-configuration"
        val metaResponse = HttpClient.get(oiddEndpoint)
        val tokenEndpoint = metaResponse.getString("token_endpoint")
        val preAuthorizationCode = credentialOfferResponse.getJSONObject("grants").getJSONObject("urn:ietf:params:oauth:grant-type:pre-authorized_code").getString("pre-authorized_code")
        val tokenRequest = hashMapOf(
            Pair("client_id", clientId),
            Pair("grant_type", "urn:ietf:params:oauth:grant-type:pre-authorized_code"),
            Pair("pre-authorized_code", preAuthorizationCode)
        )
        val tokenRequestHeaders = hashMapOf(
            Pair("content-type", "application/x-www-form-urlencoded")
        )
        val tokenResponse = HttpClient.post(tokenEndpoint, headers = tokenRequestHeaders, requestBody = tokenRequest)
        val accessToken = tokenResponse.getString("access_token")
        val cNonce = tokenResponse.getString("c_nonce")
        val vcMetaResponse = HttpClient.get(openidCredentialIssuerEndpoint)
        val credentialEndpoint = vcMetaResponse.getString("credential_endpoint")
        val credentialRequest = hashMapOf(
            Pair("format", "vc+sd-jwt"),
            Pair("vct", "https://credentials.example.com/identity_credential")
        )
        val credentialRequestHeader = hashMapOf(
            Pair("Authorization", "Bearer $accessToken")
        )
        val credentialResponse = HttpClient.post(credentialEndpoint, credentialRequestHeader, credentialRequest)
        return credentialResponse
    }

    private suspend fun getCredentialOfferResponse(url: String): JSONObject {
        if (url.contains("openid-credential-offer://?credential_offer_uri=")) {
            val encodedCredentialOfferUri = url.substring("openid-credential-offer://?credential_offer_uri=".length)
            val decodedCredentialOfferUri = URLDecoder.decode(encodedCredentialOfferUri)
            return HttpClient.get(decodedCredentialOfferUri)
        }
        val encodedCredentialOfferResponse = url.substring("openid-credential-offer://?credential_offer=".length);
        val decodedCredentialOfferResponse = URLDecoder.decode(encodedCredentialOfferResponse)
        return JSONObject(decodedCredentialOfferResponse)
    }
}