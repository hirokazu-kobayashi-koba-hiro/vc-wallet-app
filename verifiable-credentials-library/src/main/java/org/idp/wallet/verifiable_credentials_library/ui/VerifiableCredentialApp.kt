package org.idp.wallet.verifiable_credentials_library.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.idp.wallet.verifiable_credentials_library.viewmodel.VerifiableCredentialsViewModel

@Composable
fun VerifiableCredentialsApp(
    context: ComponentActivity,
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (qr: String) -> Unit,
) {
  val navController = rememberNavController()
  viewModel.filesDir = context.filesDir

  NavHost(navController = navController, startDestination = "registration") {
    composable(
        "registration",
        content = {
          WalletRegistrationView(viewModel = viewModel, goNext = { navController.navigate("main") })
        })
    composable(
        "main",
        content = {
          MainView(
              viewModel = viewModel,
              resolveQrCode = { resolveQrCode(it) },
              refreshVc = { viewModel.getAllCredentials() })
        })
  }
}
