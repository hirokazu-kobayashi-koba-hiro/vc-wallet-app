package org.idp.wallet.verifiable_credentials_library.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentialsManager
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.component.VcCardComponent
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStoreInterface
import org.idp.wallet.verifiable_credentials_library.viewmodel.VerifiableCredentialsViewModel
import java.io.File


@Preview
@Composable
fun MainPreView() {
    val walletCredentialsManager = WalletCredentialsManager(
        file = File(""),
        encryptedDataStoreInterface = object: EncryptedDataStoreInterface {
            override fun store(key: String, value: String) {

            }

            override fun find(key: String): String? {
                return null
            }

            override fun contains(key: String): Boolean {
                return false
            }

            override fun delete(key: String) {

            }

        })
    MainView(viewModel = VerifiableCredentialsViewModel(walletCredentialsManager), resolveQrCode = {}, refreshVc = {})
}
@Composable
fun MainView(
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (format: String) -> Unit,
    refreshVc: () -> Unit
) {
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
  VcWalletTheme {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
          AppDestinations.entries.forEach {
            item(
                icon = { Icon(it.icon, contentDescription = it.contentDescription) },
                label = { Text(it.label) },
                selected = it == currentDestination,
                onClick = { currentDestination = it })
          }
        },
        content = {
          when (currentDestination) {
            AppDestinations.HOME ->
                HomeScreen(viewModel = viewModel, onClick = resolveQrCode, onClickShow = refreshVc)
            AppDestinations.VC ->
                VcScreen(viewModel = viewModel, onClick = resolveQrCode, onClickShow = refreshVc)
            AppDestinations.VP -> VpScreen(viewModel = viewModel, onClick = resolveQrCode)
          }
        })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VerifiableCredentialsViewModel,
    onClick: (pinCode: String) -> Unit,
    onClickShow: () -> Unit
) {
  val vciState = viewModel.vciState.collectAsState()
  if (viewModel.loadingState.collectAsState().value) {
    LoadingScreen()
    return
  }
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        CenterAlignedTopAppBar(
            title = {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "VerifiableCredentials",
                        style = MaterialTheme.typography.displayMedium)
                    IconButton(onClick = onClickShow) {
                      Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                  }
            })
        val cardList = mutableListOf<Pair<String, String>>()
        val vc = vciState.value
        vc.entries.forEach { (key, records) ->
          records.forEach { record ->
            val stringBuilder = StringBuilder()
            record.payload.forEach {
              stringBuilder.append(it.key + ":" + it.value)
              stringBuilder.append("\n")
            }
            cardList.add(Pair(key, stringBuilder.toString()))
          }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(cardList) { (issuer, content) ->
            VcCardComponent(title = issuer, content = content)
            Log.d("VcWalletLibrary", issuer)
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VcScreen(
    viewModel: VerifiableCredentialsViewModel,
    onClick: (format: String) -> Unit,
    onClickShow: () -> Unit
) {
  if (viewModel.loadingState.collectAsState().value) {
    LoadingScreen()
    return
  }
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        var format by remember { mutableStateOf("vc+sd-jwt") }
        val vciState = viewModel.vciState.collectAsState()
        Column() {
          CenterAlignedTopAppBar(
              title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(text = "Issue Vc", style = MaterialTheme.typography.displayMedium)
                    }
              })
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                RadioButton(selected = format == "vc+sd-jwt", onClick = { format = "vc+sd-jwt" })
                Text(text = "vc-sd-jwt")
              }
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                RadioButton(selected = format == "mso_mdoc", onClick = { format = "mso_mdoc" })
                Text(text = "mso_mdoc")
              }
        }
        Row {
          Button(modifier = Modifier.padding(top = Dp(16.0F)), onClick = { onClick(format) }) {
            Text(text = "scan QR")
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpScreen(
    viewModel: VerifiableCredentialsViewModel,
    onClick: (format: String) -> Unit,
) {
  if (viewModel.loadingState.collectAsState().value) {
    LoadingScreen()
    return
  }
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        CenterAlignedTopAppBar(
            title = {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Present Vp", style = MaterialTheme.typography.displayMedium)
                  }
            })
        Row {
          Button(modifier = Modifier.padding(top = Dp(16.0F)), onClick = { onClick("vp") }) {
            Text(text = "scan QR")
          }
        }
      }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
  HOME("home", Icons.Default.Home, "home"),
  VC("vc", Icons.Default.Add, "vc"),
  VP("vp", Icons.Default.Share, "vp"),
}
