package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel

@Composable
fun WalletLauncherScreen(
    context: Context,
    openIdConnectRequest: OpenIdConnectRequest,
    forceLogin: Boolean,
    viewModel: VerifiableCredentialsViewModel,
    goNext: () -> Unit
) {
  VcWalletTheme {
    val loading = viewModel.loadingState.collectAsState()
    val login = suspend {
      val response = viewModel.login(context, openIdConnectRequest, forceLogin)
      response?.let { goNext() }
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) { login() }
    if (loading.value) {
      LoadingScreen()
    } else {
      Column(
          modifier = Modifier.fillMaxWidth().fillMaxHeight(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                  coroutineScope.launch {
                    val response = login()
                    response?.let { goNext() }
                  }
                }) {
                  Text(text = "start", style = MaterialTheme.typography.bodyMedium)
                }
          }
    }
  }
}
