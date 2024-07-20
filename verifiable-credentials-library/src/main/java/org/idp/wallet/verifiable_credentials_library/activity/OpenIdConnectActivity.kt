package org.idp.wallet.verifiable_credentials_library.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectRequestCallbackProvider
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.AuthenticationResponse
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.util.activity.open
import org.idp.wallet.verifiable_credentials_library.util.http.extractQueriesAsSingleStringMap

class OpenIdConnectActivity : ComponentActivity() {

  private val launcher =
      registerForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult(),
          callback = {
            val data = OpenIdConnectRequestCallbackProvider.callbackData
            data?.let {
              val params = extractQueriesAsSingleStringMap(it)
              OpenIdConnectRequestCallbackProvider.callback.onSuccess(
                  AuthenticationResponse(params))
              OpenIdConnectRequestCallbackProvider.callbackData = null
              finish()
              return@registerForActivityResult
            }
            OpenIdConnectRequestCallbackProvider.callback.onFailure()
            finish()
          })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LoadingScreen() }
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
      Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
      OpenIdConnectRequestCallbackProvider.callback.onFailure()
      finish()
    }
    lifecycleScope.launch(errorHandler) {
      val authenticationRequestUri = intent.getStringExtra("authenticationRequestUri").toString()
      open(launcher = launcher, uri = authenticationRequestUri)
    }
    onBackPressedDispatcher.addCallback {
      OpenIdConnectRequestCallbackProvider.callback.onFailure()
      finish()
    }
  }
}
