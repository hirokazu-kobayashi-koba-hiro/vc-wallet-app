package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.ui.component.SystemDialog
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel

@Composable
fun VerifiableCredentialsApp(
    context: Context,
    viewModel: VerifiableCredentialsViewModel,
    openIdConnectRequest: OpenIdConnectRequest,
    forceLogin: Boolean,
    resolveQrCode: (qr: String) -> Unit,
) {
  val navController = rememberNavController()
  val systemDialogState = viewModel.systemDialogState.collectAsState()
  NavHost(navController = navController, startDestination = "launcher") {
    composable(
        "launcher",
        content = {
          WalletLauncherScreen(
              context,
              openIdConnectRequest,
              forceLogin,
              viewModel,
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
              goNext = { seed -> navController.navigate("wallet-seed-confirmation?seed=$seed") },
              goNextToRestore = { navController.navigate("wallet-restore") },
          )
        })
    composable(
        "wallet-restore",
        content = {
          WalletRestoreScreen(
              createCredential = { password: String, seed: String ->
                return@WalletRestoreScreen viewModel.restoreCredential(password, seed)
              },
              goNext = { navController.navigate("main") })
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
          VerifiableCredentialsMainScreen(
              navController,
              viewModel = viewModel,
              resolveQrCode = { resolveQrCode(it) },
          )
        })
    composable(
        "account-detail",
        content = {
          AccountDetailScreen(
              userinfoResponse =
                  viewModel.loginState.value.userinfoResponse ?: UserinfoResponse(sub = ""))
        })
    composable(
        "wallet-key", content = { WalletKeyScreen(credentials = viewModel.findCredential()!!) })
  }
  SystemDialog(systemDialogState = systemDialogState.value)
}
