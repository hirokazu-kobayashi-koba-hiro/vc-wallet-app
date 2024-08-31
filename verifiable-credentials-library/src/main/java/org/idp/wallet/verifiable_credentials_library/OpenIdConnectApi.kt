package org.idp.wallet.verifiable_credentials_library

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.widget.AppCompatEditText
import java.util.UUID
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
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.PushAuthenticationResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.domain.user.User
import org.idp.wallet.verifiable_credentials_library.domain.user.UserRepository
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

/**
 * Provides an API for handling OpenID Connect operations. This object manages the authentication
 * process, token handling, and user management for OpenID Connect requests.
 */
object OpenIdConnectApi {

  private val tokenRegistry = TokenRegistry()
  private lateinit var userRepository: UserRepository

  /**
   * Initializes the OpenIdConnectApi with the user repository.
   *
   * @param userRepository The repository used to manage user data.
   */
  fun initialize(userRepository: UserRepository) {
    this.userRepository = userRepository
  }

  /**
   * Performs the login process using OpenID Connect.
   *
   * Depending on the token direction determined by the [TokenDirector], this method either uses a
   * cached token, issues a new token, or refreshes an existing token. It also manages the user's
   * information in the user repository.
   *
   * @param context The application context.
   * @param request The OpenID Connect request containing the necessary parameters for login.
   * @param force Indicates whether to force the login process regardless of cached tokens.
   * @return An [OpenIdConnectResponse] containing the result of the login process.
   * @throws OpenIdConnectException if an unexpected error occurs during the login process.
   */
  suspend fun login(
      context: Context,
      request: OpenIdConnectRequest,
      force: Boolean = false
  ): OpenIdConnectResponse {

    val response = getOpenIdConnectResponse(context, request, force)

    val foundUser = userRepository.find(response.sub())
    foundUser?.let {
      val user = response.toUser(it.id)
      userRepository.update(user)
    }
        ?: run {
          val userId = UUID.randomUUID().toString()
          val user = response.toUser(userId)
          userRepository.register(user)
        }

    return response
  }

  /**
   * Retrieves the current logged-in user.
   *
   * @return The [User] object representing the current user.
   */
  suspend fun getCurrentUser(): User {
    return userRepository.getCurrentUser()
  }

  /**
   * Determines the appropriate action based on the token direction and executes the corresponding
   * flow to retrieve the OpenID Connect response.
   *
   * This method handles different token directions including using a cached token, issuing a new
   * token, or refreshing an existing token. It validates responses and manages token storage and
   * validation.
   *
   * @param context The application context.
   * @param request The OpenID Connect request containing the necessary parameters.
   * @param force Indicates whether to force the login process regardless of cached tokens.
   * @return The [OpenIdConnectResponse] containing the result of the authentication process.
   * @throws OpenIdConnectException if an unexpected error occurs during the process.
   */
  private suspend fun getOpenIdConnectResponse(
      context: Context,
      request: OpenIdConnectRequest,
      force: Boolean,
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

        val authenticationRequestUri =
            "${oidcMetadata.authorizationEndpoint}${request.queries(forceLogin = force)}"
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
    throw OpenIdConnectException(
        OidcError.UNEXPECTED_ERROR,
        String.format("unexpected error on login, unsupported token direction %s.", direction.name))
  }

  /**
   * Retrieves OpenID Connect metadata from the specified URL.
   *
   * @param url The URL from which to retrieve the OpenID Connect metadata.
   * @return The [OidcMetadata] object containing OpenID Connect metadata.
   */
  suspend fun getOidcMetadata(url: String): OidcMetadata {

    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), OidcMetadata::class.java)
  }

  /**
   * Requests a token using the provided token request parameters.
   *
   * @param url The token endpoint URL.
   * @param request The [TokenRequest] containing the parameters for the token request.
   * @return The [TokenResponse] containing the token data.
   */
  suspend fun requestToken(url: String, request: TokenRequest): TokenResponse {

    val response = HttpClient.post(url, requestBody = request.values())
    val tokenResponse = JsonUtils.read(response.toString(), TokenResponse::class.java)
    return tokenResponse
  }

  /**
   * Retrieves the JSON Web Key Set (JWKS) from the specified URL.
   *
   * @param url The URL from which to retrieve the JWKS.
   * @return The [JwksResponse] containing the JWKS data.
   */
  suspend fun getJwks(url: String): JwksResponse {

    val response = HttpClient.get(url)
    return JwksResponse(response)
  }

  /**
   * Retrieves the user information from the specified userinfo endpoint.
   *
   * @param url The userinfo endpoint URL.
   * @param accessToken The access token used for authorization.
   * @return The [UserinfoResponse] containing the user information.
   */
  suspend fun getUserinfo(url: String, accessToken: String): UserinfoResponse {

    val headers = mapOf("Authorization" to "Bearer $accessToken")
    val response = HttpClient.get(url, headers = headers)
    val userinfoResponse = JsonUtils.read(response.toString(), UserinfoResponse::class.java)
    return userinfoResponse
  }

  /**
   * Sends a push authentication request to the specified URL.
   *
   * @param url The URL to which the push authentication request is sent.
   * @param dpopJwt The DPoP JWT used for the request, if available.
   * @param body The request body containing the necessary parameters.
   * @return The [PushAuthenticationResponse] containing the result of the push authentication
   *   request.
   */
  suspend fun pushAuthenticationRequest(
      url: String,
      dpopJwt: String?,
      body: Map<String, Any>
  ): PushAuthenticationResponse {

    val headers = mutableMapOf(Pair("content-type", "application/x-www-form-urlencoded"))
    dpopJwt?.let { headers.put("DPoP", it) }
    val response = HttpClient.post(url, headers = headers, requestBody = body)
    val pushAuthenticationResponse =
        JsonUtils.read(response.toString(), PushAuthenticationResponse::class.java)
    return pushAuthenticationResponse
  }

  /**
   * Handles the OpenID Connect authentication request and returns the authentication response.
   *
   * This method launches an activity to handle the authentication process and uses a callback to
   * receive the result of the authentication.
   *
   * @param context The application context.
   * @param authenticationRequestUri The URI for the authentication request.
   * @return The [AuthenticationResponse] containing the result of the authentication request.
   */
  internal suspend fun request(
      context: Context,
      authenticationRequestUri: String
  ): AuthenticationResponse = suspendCoroutine { continuation ->
    val callback =
        object : OpenidConnectRequestCallback {
          override fun onSuccess(response: AuthenticationResponse) {
            continuation.resume(response)
          }

          override fun onFailure() {
            val editText =
                AppCompatEditText(context).apply {
                  hint = "code"
                  setPadding(40, 40, 40, 40)
                }
            AlertDialog.Builder(context)
                .setTitle("Temporary Input Dialog")
                .setMessage("please input code")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                  val response =
                      AuthenticationResponse(values = mapOf("code" to editText.text.toString()))
                  continuation.resume(response)
                  dialog.dismiss()
                }
                .setNegativeButton("CANCEL") { dialog, _ ->
                  continuation.resumeWithException(
                      OpenIdConnectException(OidcError.NOT_AUTHENTICATED))
                  dialog.dismiss()
                }
                .show()
          }
        }

    OpenIdConnectRequestCallbackProvider.callback = callback
    val intent = Intent(context, OpenIdConnectActivity::class.java)
    intent.putExtra("authenticationRequestUri", authenticationRequestUri)
    context.startActivity(intent)
  }
}

/**
 * Provides a callback mechanism for handling OpenID Connect request responses. This object is used
 * to register and manage callbacks for OpenID Connect authentication requests.
 */
object OpenIdConnectRequestCallbackProvider {

  lateinit var callback: OpenidConnectRequestCallback
  var callbackData: String? = null
}

/**
 * Defines the callback interface for OpenID Connect request responses. Implementations of this
 * interface are used to handle the success or failure of OpenID Connect authentication requests.
 */
interface OpenidConnectRequestCallback {

  /**
   * Called when the OpenID Connect request is successful.
   *
   * @param response The [AuthenticationResponse] containing the result of the request.
   */
  fun onSuccess(response: AuthenticationResponse)

  /** Called when the OpenID Connect request fails. */
  fun onFailure()
}
