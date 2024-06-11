package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.content.Intent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.error.OidcError
import org.idp.wallet.verifiable_credentials_library.domain.error.OpenIdConnectException
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.AuthenticationResponseValidator
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.OpenIdConnectResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.AuthenticationResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.ui.OpenIdConnectActivity
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

object OpenIdConnectApi {
  suspend fun login(
      context: Context,
      request: OpenIdConnectRequest,
      force: Boolean = false
  ): OpenIdConnectResponse {
    val oidcMetadata = getOidcMetadata("${request.issuer}/.well-known/openid-configuration/")
    val authenticationRequestUri = "${oidcMetadata.authorizationEndpoint}${request.queries()}"
    val response = request(context, authenticationRequestUri)
    val authenticationResponseValidator = AuthenticationResponseValidator(request, response)
    authenticationResponseValidator.validate()
    val tokenResponse =
        requestToken(
            oidcMetadata.tokenEndpoint, request.clientId, response.code(), request.redirectUri)
    val userinfoResponse = getUserinfo(oidcMetadata.userinfoEndpoint, tokenResponse.accessToken)
    return OpenIdConnectResponse(tokenResponse, userinfoResponse)
  }

  suspend fun getOidcMetadata(url: String): OidcMetadata {
    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), OidcMetadata::class.java)
  }

  suspend fun requestToken(
      url: String,
      clientId: String,
      code: String,
      redirectUri: String
  ): TokenResponse {
    val request =
        mapOf(
            "client_id" to clientId,
            "code" to code,
            "redirect_uri" to redirectUri,
            "grant_type" to "authorization_code")
    val response = HttpClient.post(url, requestBody = request)
    val tokenResponse = JsonUtils.read(response.toString(), TokenResponse::class.java)
    return tokenResponse
  }

  suspend fun getUserinfo(url: String, accessToken: String): UserinfoResponse {
    val headers = mapOf("Authorization" to "Bearer $accessToken")
    val response = HttpClient.get(url, headers = headers)
    val userinfoResponse = JsonUtils.read(response.toString(), UserinfoResponse::class.java)
    return userinfoResponse
  }

  private suspend fun request(
      context: Context,
      authenticationRequestUri: String
  ): AuthenticationResponse = suspendCoroutine { continuation ->
    val callback =
        object : OpenidConnectRequestCallback {
          override fun onSuccess(response: AuthenticationResponse) {
            continuation.resume(response)
          }

          override fun onFailure() {
            continuation.resumeWithException(OpenIdConnectException(OidcError.NOT_AUTHENTICATED))
          }
        }
    OpenIdConnectRequestCallbackProvider.callback = callback
    val intent = Intent(context, OpenIdConnectActivity::class.java)
    intent.putExtra("authenticationRequestUri", authenticationRequestUri)
    context.startActivity(intent)
  }
}

interface OpenidConnectCallback {
  fun onSuccess()

  fun onFailure()
}

object OpenIdConnectRequestCallbackProvider {
  lateinit var callback: OpenidConnectRequestCallback
}

interface OpenidConnectRequestCallback {
  fun onSuccess(response: AuthenticationResponse)

  fun onFailure()
}
