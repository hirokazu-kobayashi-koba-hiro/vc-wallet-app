package org.idp.wallet.verifiable_credentials_library.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectRequestCallbackProvider
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen

class OpenIdConnectRedirectActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LoadingScreen() }
    OpenIdConnectRequestCallbackProvider.callbackData = intent.data.toString()
    val intent = Intent(this, OpenIdConnectActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish()
  }
}
