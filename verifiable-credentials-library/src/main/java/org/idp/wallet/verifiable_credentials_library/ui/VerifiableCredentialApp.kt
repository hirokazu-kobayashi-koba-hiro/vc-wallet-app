package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.idp.wallet.verifiable_credentials_library.viewmodel.VerifiableCredentialsViewModel

@Composable
fun VerifiableCredentialsApp(
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (qr: String) -> Unit,
) {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "launcher") {
    composable(
        "launcher",
        content = {
          WalletLauncherView(
              goNext = {
                val credential = viewModel.findCredential()
                if (credential == null) {
                  navController.navigate("wallet-registration")
                } else {
                  navController.navigate("main")
                }
              })
        })
    composable(
        "wallet-registration",
        content = {
          WalletRegistrationView(
              viewModel = viewModel,
              goNext = { seed -> navController.navigate("wallet-seed-confirmation?seed=$seed") })
        })
    composable(
        "wallet-seed-confirmation?seed={seed}",
        arguments = listOf(navArgument("seed") { defaultValue = "" }),
        content = {
          val seed = it.arguments?.getString("seed")
          WalletSeedConfirmationView(seed = seed ?: "", goNext = { navController.navigate("main") })
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
