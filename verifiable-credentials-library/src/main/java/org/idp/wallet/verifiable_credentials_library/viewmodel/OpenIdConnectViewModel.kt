package org.idp.wallet.verifiable_credentials_library.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.json.JSONObject

class OpenIdConnectViewModel : ViewModel() {

  var _credential = MutableStateFlow(JSONObject())
  val credential = _credential.asStateFlow()

  suspend fun getOidcMetadata(url: String): OidcMetadata {
    val response = HttpClient.get(url)
    return JsonUtils.read(response.toString(), OidcMetadata::class.java)
  }
  suspend fun requestToken(url: String, clientId: String, code: String, redirectUri: String) {
    val request = mapOf("client_id" to clientId, "code" to code, "redirect_uri" to redirectUri, "grant_type" to "authorization_code")
    val response = HttpClient.post(url, requestBody = request)
    _credential.value = response
  }
}
