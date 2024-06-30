package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.idp.wallet.verifiable_credentials_library.ui.component.SystemDialog
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel

@Composable
fun VerifiableCredentialsApp(
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (qr: String) -> Unit,
) {
  val navController = rememberNavController()
  var systemDialogState = viewModel.systemDialogState.collectAsState()
  NavHost(navController = navController, startDestination = "launcher") {
    composable(
        "launcher",
        content = {
          WalletLauncherScreen(
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
          WalletRegistrationScreen(
              createCredential = { password: String ->
                return@WalletRegistrationScreen viewModel.createCredential(password)
              },
              goNext = { seed -> navController.navigate("wallet-seed-confirmation?seed=$seed") })
        })
    composable(
        "wallet-seed-confirmation?seed={seed}",
        arguments = listOf(navArgument("seed") { defaultValue = "" }),
        content = {
          val seed = it.arguments?.getString("seed")
          WalletSeedConfirmationScreen(
              seed = seed ?: "", goNext = { navController.navigate("main") })
        })
    composable(
        "main",
        content = {
          MainScreen(
              viewModel = viewModel,
              resolveQrCode = { resolveQrCode(it) },
              refreshVc = { viewModel.getAllCredentials() })
        })
  }
  SystemDialog(systemDialogState = systemDialogState.value)
}
