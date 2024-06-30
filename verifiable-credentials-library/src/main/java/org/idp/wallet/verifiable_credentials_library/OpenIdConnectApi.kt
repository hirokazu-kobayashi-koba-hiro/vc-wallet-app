package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.content.Intent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.activity.OpenIdConnectActivity
import org.idp.wallet.verifiable_credentials_library.domain.error.OidcError
import org.idp.wallet.verifiable_credentials_library.domain.error.OpenIdConnectException
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.AuthenticationResponseValidator
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.IdTokenValidator
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.OpenIdConnectResponse
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.TokenDirection
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.TokenDirector
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.TokenRecord
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.TokenRegistry
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenRequest
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.AuthenticationResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.JwksResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

object OpenIdConnectApi {

  private val tokenRegistry = TokenRegistry()

  suspend fun login(
      context: Context,
      request: OpenIdConnectRequest,
      force: Boolean = false
  ): OpenIdConnectResponse {
    val tokenRecord = tokenRegistry.find(request.scope)
    val tokenDirector = TokenDirector(force, tokenRecord)
    val direction = tokenDirector.direct()
    val oidcMetadata = getOidcMetadata("${request.issuer}/.well-known/openid-configuration/")
    when (direction) {
      TokenDirection.CACHE -> {
        tokenRecord?.let {
          if (request.containsOidcInScope()) {
            val userinfoResponse =
                getUserinfo(oidcMetadata.userinfoEndpoint, it.tokenResponse.accessToken)
            return OpenIdConnectResponse(it.tokenResponse, userinfoResponse)
          }
          return OpenIdConnectResponse(it.tokenResponse)
        }
      }
      TokenDirection.ISSUE -> {
        val authenticationRequestUri = "${oidcMetadata.authorizationEndpoint}${request.queries()}"
        val response = request(context, authenticationRequestUri)
        val authenticationResponseValidator = AuthenticationResponseValidator(request, response)
        authenticationResponseValidator.validate()
        val tokenRequest =
            TokenRequest(
                clientId = request.clientId,
                grantType = "authorization_code",
                code = response.code(),
                redirectUri = request.redirectUri,
            )
        val tokenResponse = requestToken(oidcMetadata.tokenEndpoint, tokenRequest)
        tokenRegistry.add(request.scope, TokenRecord(tokenResponse, 3600))
        if (request.containsOidcInScope()) {
          val jwkResponse = getJwks(oidcMetadata.jwksUri)
          val idTokenValidator = IdTokenValidator(request, tokenResponse, jwkResponse)
          idTokenValidator.validate()
          val userinfoResponse =
              getUserinfo(oidcMetadata.userinfoEndpoint, tokenResponse.accessToken)
          return OpenIdConnectResponse(tokenResponse, userinfoResponse)
        }
        return OpenIdConnectResponse(tokenResponse)
      }
      TokenDirection.REFRESH -> {
        tokenRecord?.let {
          val tokenRequest =
              TokenRequest(
                  clientId = request.clientId,
                  grantType = "refresh_token",
                  refreshToken = it.tokenResponse.refreshToken)
          val tokenResponse = requestToken(oidcMetadata.tokenEndpoint, tokenRequest)
          val refreshedTokenRecord = it.refresh(tokenResponse)
          tokenRegistry.add(request.scope, refreshedTokenRecord)
          if (request.containsOidcInScope()) {
            val userinfoResponse =
                getUserinfo(oidcMetadata.userinfoEndpoint, tokenResponse.accessToken)
            return OpenIdConnectResponse(tokenResponse, userinfoResponse)
          }
          return OpenIdConnectResponse(tokenResponse)
        }
      }
    }
    throw RuntimeException("unexpected error on login")
  }

  suspend fun getOidcMetadata(url: String): OidcMetadata {
    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), OidcMetadata::class.java)
  }

  suspend fun requestToken(url: String, request: TokenRequest): TokenResponse {
    val response = HttpClient.post(url, requestBody = request.values())
    val tokenResponse = JsonUtils.read(response.toString(), TokenResponse::class.java)
    return tokenResponse
  }

  suspend fun getJwks(url: String): JwksResponse {
    val response = HttpClient.get(url)
    return JwksResponse(response)
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
