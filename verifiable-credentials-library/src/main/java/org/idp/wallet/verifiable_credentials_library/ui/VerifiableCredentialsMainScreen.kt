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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import java.io.File
import org.idp.wallet.verifiable_credentials_library.R
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentialsManager
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.component.VcCardComponent
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStoreInterface

@Preview
@Composable
fun MainPreView() {
  val walletCredentialsManager =
      WalletCredentialsManager(
          file = File(""),
          encryptedDataStoreInterface =
              object : EncryptedDataStoreInterface {
                override fun store(key: String, value: String) {}

                override fun find(key: String): String? {
                  return null
                }

                override fun contains(key: String): Boolean {
                  return false
                }

                override fun delete(key: String) {}
              })
  MainScreen(
      viewModel = VerifiableCredentialsViewModel(walletCredentialsManager),
      resolveQrCode = {},
      refreshVc = {})
}

@Composable
fun MainScreen(
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
                HomeScreen(
                    viewModel = viewModel, gotoDetail = resolveQrCode, onClickShow = refreshVc)
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
    gotoDetail: (id: String) -> Unit,
    onClickShow: () -> Unit
) {
  val vciState = viewModel.vciState.collectAsState()
  val loginState = viewModel.loginState.collectAsState()
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
        loginState.value.userinfoResponse?.let {
          Text(text = it.sub)
          Text(text = it.name ?: "")
        }
        val cardList = mutableListOf<Pair<String, VerifiableCredentialsRecord>>()
        val vc = vciState.value
        vc.entries.forEach { (key, records) ->
          records.forEach { record -> cardList.add(Pair(record.type, record)) }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(cardList) { (type, record) ->
            VcCardComponent(
                icon = R.drawable.id_card,
                title = type,
                detailContent = { CardDetailContent(record = record) })
            Log.d("VcWalletLibrary", type)
          }
        }
      }
}

@Composable
fun CardDetailContent(record: VerifiableCredentialsRecord) {
  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
        record.payload.entries.forEach {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = it.key,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = Dp(16.0F)))
                Text(text = it.value.toString(), style = MaterialTheme.typography.bodyMedium)
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
          Button(
              modifier = Modifier.padding(top = Dp(16.0F)),
              onClick = {
                viewModel.showDialog(
                    title = "confirm",
                    message = "Could you scan qr?",
                    onClickPositiveButton = { onClick(format) })
              }) {
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
