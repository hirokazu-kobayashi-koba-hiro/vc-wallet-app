package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectRequestCallbackProvider
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OidcMetadata
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.util.activity.open
import org.idp.wallet.verifiable_credentials_library.util.http.extractQueriesAsSingleStringMap
import org.idp.wallet.verifiable_credentials_library.viewmodel.OpenIdConnectViewModel

class OpenIdConnectActivity : ComponentActivity() {

  val viewModel: OpenIdConnectViewModel by lazy {
    ViewModelProvider(this).get(OpenIdConnectViewModel::class.java)
  }
  private val launcher =
      registerForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult(),
          callback = {
//            OpenIdConnectRequestCallbackProvider.callback.onFailure()
//            finish()
          })
  lateinit var clientId: String
  lateinit var redirectUri: String
  lateinit var oidcMetadata: OidcMetadata

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LoadingScreen() }
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
      Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
      OpenIdConnectRequestCallbackProvider.callback.onFailure()
      finish()
    }
    lifecycleScope.launch(errorHandler) {
      val issuer = intent.getStringExtra("issuer").toString()
      clientId = intent.getStringExtra("clientId").toString()
      redirectUri = intent.getStringExtra("redirectUri").toString()
      val queries = intent.getStringExtra("queries").toString()
      oidcMetadata = viewModel.getOidcMetadata("$issuer/.well-known/openid-configuration/")
      val authenticationRequestUri = oidcMetadata.authorizationEndpoint + queries
      open(launcher = launcher, uri = authenticationRequestUri)
    }
    onBackPressedDispatcher.addCallback {
      OpenIdConnectRequestCallbackProvider.callback.onFailure()
      finish()
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val data = intent.data
    data?.let {
      lifecycleScope.launch {
        val params = extractQueriesAsSingleStringMap(it)
        Log.d("OpenIdConnectActivity", it.toString())
        val tokenEndpoint = oidcMetadata.tokenEndpoint
        val code = params["code"] ?: ""
        viewModel.requestToken(
          url = tokenEndpoint,
          clientId = clientId,
          code = code,
          redirectUri = redirectUri)
        OpenIdConnectRequestCallbackProvider.callback.onSuccess()
        finish()
      }
    } ?: {
      OpenIdConnectRequestCallbackProvider.callback.onFailure()
      finish()
    }
  }
}
