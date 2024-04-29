package org.idp.wallet.verifiable_credentials_library.oauth

import android.content.Context
import android.net.Uri
import org.idp.wallet.verifiable_credentials_library.basic.activity.move
import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.json.JSONObject

class AuthorizationResponseCallbackService(
    private val context: Context,
    private val authorizationContext: OAuthRequestContext,
    private val authorizationResponse: AuthorizationResponse
) {

  suspend fun callback() {
    if (authorizationContext.isDirectPost()) {
      val response = post()
      val redirectUri = response["redirect_uri"] as String
      val responseCode = response["response_code"] as String
      val url =
          Uri.parse(redirectUri)
              .buildUpon()
              .appendQueryParameter("response_code", responseCode)
              .toString()
      moveToVerifier(url)
    } else {
      moveToVerifier(authorizationResponse.redirectUriValue())
    }
  }

  private fun moveToVerifier(uri: String) {
    move(context, uri)
  }

  private suspend fun post(): JSONObject {
    val url = authorizationResponse.redirectUri
    val headers = mutableMapOf("content-type" to "application/x-www-form-urlencoded")
    val params = authorizationResponse.prams()
    return HttpClient.post(url = url, headers = headers, requestBody = params)
  }
}
