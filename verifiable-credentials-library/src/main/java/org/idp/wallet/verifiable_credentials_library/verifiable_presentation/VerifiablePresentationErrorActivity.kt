package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.ui.SingleButtonPage
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

class OAuthErrorActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { OAuthErrorView(onClick = { finish() }) }
  }

  override fun onDestroy() {
    super.onDestroy()
    OAuthErrorActivityCallbackProvider.callback.finish()
  }
}

@Preview
@Composable
fun OAuthErrorPreView() {
  OAuthErrorView(onClick = {})
}

@Composable
fun OAuthErrorView(onClick: () -> Unit) {
  VcWalletTheme {
    SingleButtonPage(
        title = "Error",
        content = {
          Text(text = "something went wrong", style = MaterialTheme.typography.bodyLarge)
        },
        buttonLabel = "Close",
        onClick)
  }
}

class OAuthErrorActivityWrapper {

  companion object {
    suspend fun launch(context: Context, errorDescription: String? = "") = suspendCoroutine {
      val callbackHandler =
          object : OAuthErrorActivityCallback {
            override fun finish() {
              it.resume(Unit)
            }
          }
      OAuthErrorActivityCallbackProvider.callback = callbackHandler
      val intent = Intent(context, OAuthErrorActivity::class.java)
      context.startActivity(intent)
    }
  }
}

object OAuthErrorActivityCallbackProvider {
  lateinit var callback: OAuthErrorActivityCallback
}

interface OAuthErrorActivityCallback {
  fun finish()
}
