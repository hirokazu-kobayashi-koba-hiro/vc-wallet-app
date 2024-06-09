package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.content.Intent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.ui.OpenIdConnectActivity

object OpenIdConnectApi {
  suspend fun connect(context: Context, request: OpenIdConnectRequest) {
    val authenticationRequestUri = request.authenticationRequestUri()
    val requestResponse = request(context, authenticationRequestUri)
  }

  private suspend fun request(context: Context, authenticationRequestUri: String): Boolean =
      suspendCoroutine { continuation ->
        val callback =
            object : OpenidConnectRequestCallback {
              override fun onSuccess() {
                continuation.resume(true)
              }

              override fun onFailure() {
                continuation.resume(false)
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
  fun onSuccess()

  fun onFailure()
}
