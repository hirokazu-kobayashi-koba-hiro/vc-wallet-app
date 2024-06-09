package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.content.Intent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.ui.OpenIdConnectActivity

object OpenIdConnectApi {
  suspend fun connect(context: Context, request: OpenIdConnectRequest): Boolean {
    return request(context, request)
  }

  private suspend fun request(context: Context, request: OpenIdConnectRequest): Boolean =
      suspendCoroutine { continuation ->
        val queries = request.queries()
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
        intent.putExtra("issuer", request.issuer)
        intent.putExtra("clientId", request.clientId)
        intent.putExtra("redirectUri", request.redirectUri)
        intent.putExtra("queries", queries)
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
