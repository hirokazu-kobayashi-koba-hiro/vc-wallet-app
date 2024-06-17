package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import org.idp.wallet.verifiable_credentials_library.util.activity.move
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.json.JSONObject

class AuthorizationResponseCallbackService(
    private val context: Context,
    private val authorizationContext: VerifiablePresentationRequestContext,
    private val authorizationResponse: AuthorizationResponse
) {

  suspend fun callback() {
    if (authorizationContext.isDirectPost()) {
      Log.d("VcWalletLibrary", "response mode is direct post")
      val response = post()
      val redirectUri = response.optString("redirect_uri", "")
      val responseCode = response.optString("response_code", "")
      if (redirectUri.isNotEmpty()) {
        val uriBuilder = Uri.parse(redirectUri).buildUpon()
        if (responseCode.isNotEmpty()) {
          uriBuilder.appendQueryParameter("response_code", responseCode)
        }
        moveToVerifier(uriBuilder.toString())
      }
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
