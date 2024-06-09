package org.idp.wallet.verifiable_credentials_library.viewmodel

import androidx.lifecycle.ViewModel
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient

class OpenIdConnectViewModel : ViewModel() {

  suspend fun requestToken(clientId: String, code: String) {
    val tokenResponse = HttpClient.post("")
  }
}
