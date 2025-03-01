package org.idp.wallet.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest

class LauncherActivity : FragmentActivity() {

  private val viewModel: LauncherViewModel by lazy {
    ViewModelProvider(this).get(LauncherViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LauncherView(viewModel = viewModel, onClick = { login() }) }
    VerifiableCredentialsClient.initialize(this, WalletConfiguration(issuer = "vc-wallet"))
    login()
  }

  private fun login() {
    val request =
        OpenIdConnectRequest(
            issuer = getString(R.string.com_vc_wallet_issuer),
            clientId = getString(R.string.com_vc_wallet_client_id),
            scope = "openid profile phone email address offline_access",
            state = UUID.randomUUID().toString(),
            nonce = UUID.randomUUID().toString(),
            redirectUri =
            "org.idp.verifiable.credentials://${getString(R.string.com_vc_wallet_domain)}/android/org.idp.wallet.app/callback")
    VerifiableCredentialsClient.start(context = this, request = request, forceLogin = true)
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
  LauncherView(viewModel = LauncherViewModel(), onClick = {})
}

@Composable
fun LauncherView(viewModel: LauncherViewModel, onClick: () -> Unit) {
  val loading = viewModel.loadingState.collectAsState()
  Column(
      modifier = Modifier.fillMaxWidth().fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (loading.value) {
          CircularProgressIndicator()
          return
        }
        Button(onClick = onClick) {
          Text(text = "start", style = MaterialTheme.typography.bodyMedium)
        }
      }
}
