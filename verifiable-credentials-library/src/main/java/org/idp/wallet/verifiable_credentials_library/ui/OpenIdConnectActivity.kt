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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val data = intent.data
    data?.let {
      val query = extractQueriesAsSingleStringMap(it)
      Log.d("OpenIdConnectActivity", it.toString())
    }
    OpenIdConnectRequestCallbackProvider.callback.onSuccess()
    finish()
  }
}
