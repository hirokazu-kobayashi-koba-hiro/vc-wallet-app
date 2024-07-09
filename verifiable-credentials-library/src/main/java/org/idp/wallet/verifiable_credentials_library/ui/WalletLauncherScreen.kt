package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel

@Composable
fun WalletLauncherScreen(
    context: Context,
    openIdConnectRequest: OpenIdConnectRequest,
    forceLogin: Boolean,
    viewModel: VerifiableCredentialsViewModel,
    goNext: () -> Unit
) {
  LoadingScreen(color = MaterialTheme.colorScheme.primary)
  LaunchedEffect(Unit) {
    viewModel.login(context, openIdConnectRequest, forceLogin)
    goNext()
  }
}
