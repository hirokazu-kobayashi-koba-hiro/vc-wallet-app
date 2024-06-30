package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.idp.wallet.verifiable_credentials_library.viewmodel.VerifiableCredentialsViewModel


@Composable
fun VerifiableCredentialsApp(
    context: Context,
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (qr: String) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "registration") {
        composable("registration", content = {
            WalletRegistrationView(goNext = { password ->
                viewModel.createKeyPair(password, context.filesDir)
                navController.navigate("main")
            })
        })
        composable("main", content = {
            MainView(
                viewModel = viewModel,
                resolveQrCode = {
                  resolveQrCode(it)
                },
                refreshVc = {
                    viewModel.getAllCredentials()
            })
        })
    }
}