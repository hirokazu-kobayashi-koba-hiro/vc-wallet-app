package org.idp.wallet.verifiable_credentials_library.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import java.io.File
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.R
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentialsManager
import org.idp.wallet.verifiable_credentials_library.ui.component.CardComponent
import org.idp.wallet.verifiable_credentials_library.ui.component.FloatingView
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStoreInterface

@Preview
@Composable
fun MainPreView() {
  val context = LocalContext.current
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
  VerifiableCredentialsMainScreen(
      navController = NavHostController(context),
      viewModel = VerifiableCredentialsViewModel(walletCredentialsManager),
      resolveQrCode = {},
  )
}

@Composable
fun VerifiableCredentialsMainScreen(
    navController: NavHostController,
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (format: String) -> Unit,
) {
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
  val context = LocalContext.current
  val scope = CoroutineScope(Dispatchers.Main)
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
                    navController = navController,
                    viewModel = viewModel,
                    gotoDetail = resolveQrCode)
            AppDestinations.VC ->
                VcScreen(
                    viewModel = viewModel,
                    resolveQrCode = resolveQrCode,
                    issueVcWhenAuthorizationCodeFlow = {
                      val errorHandler =
                          CoroutineExceptionHandler(
                              handler = { _, error ->
                                viewModel.showDialog(
                                    title = "Error", message = error.message ?: "unexpected error")
                              })
                      scope.launch(errorHandler) {
                        viewModel.requestVcOnAuthorizationCode(
                            context = context, it, "org.iso.18013.5.1.mDL")
                      }
                    })
            AppDestinations.VP -> VpScreen(viewModel = viewModel, resolveQrCode = resolveQrCode)
          }
        })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: VerifiableCredentialsViewModel,
    gotoDetail: (id: String) -> Unit,
) {
  val vciState = viewModel.vciState.collectAsState()
  val loginState = viewModel.loginState.collectAsState()
  var showFloatingScreen by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { viewModel.getAllCredentials() }

  Scaffold(
      modifier = Modifier.fillMaxWidth(),
      topBar = {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
          CenterAlignedTopAppBar(
              title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Spacer(modifier = Modifier.padding())
                      Text(
                          text = "VerifiableCredentials",
                          style = MaterialTheme.typography.displayMedium)
                      IconButton(onClick = { showFloatingScreen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                      }
                    }
              })
        }
      },
      content = { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
          loginState.value.userinfoResponse?.let {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
                  Text(text = it.sub)
                  Text(text = it.name ?: "")
                }
          }
          val cardList = mutableListOf<Pair<String, VerifiableCredentialsRecord>>()
          val vc = vciState.value
          vc.forEach { record -> cardList.add(Pair(record.type, record)) }
          LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(cardList) { (type, record) ->
              CardComponent(
                  icon = {
                    Image(
                        painter = painterResource(id = R.drawable.id_card),
                        contentDescription = "contentDescription",
                        modifier = Modifier.size(Dp(50.0F)))
                  },
                  title = type,
                  detailContent = { CardDetailContent(record = record) })
              Log.d("VcWalletLibrary", type)
            }
          }
        }
      })
  FloatingView(
      visible = showFloatingScreen,
      start = Dp(40.0F),
      top = Dp(40.0F),
      content = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Button(
              shape = RoundedCornerShape(Dp(0F)),
              modifier = Modifier.fillMaxWidth(),
              colors =
                  ButtonDefaults.buttonColors(
                      contentColor = Color.Black, containerColor = Color.White),
              onClick = { navController.navigate("account-detail") },
              content = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(8.0F)),
                    content = {
                      Icon(Icons.Default.AccountCircle, contentDescription = "AccountCircle")
                      Text(text = "account")
                    })
              })
          Button(
              shape = RoundedCornerShape(Dp(0F)),
              colors =
                  ButtonDefaults.buttonColors(
                      contentColor = Color.Black, containerColor = Color.White),
              onClick = { navController.navigate("wallet-key") },
              content = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(8.0F)),
                    content = {
                      Icon(Icons.Default.Key, contentDescription = "Key")
                      Text(text = "wallet-key")
                    })
              })
        }
      },
      onDismiss = { showFloatingScreen = false })
}

@Composable
private fun CardDetailContent(record: VerifiableCredentialsRecord) {
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

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
  HOME("home", Icons.Default.Home, "home"),
  VC("vc", Icons.Default.Add, "vc"),
  VP("vp", Icons.Default.Share, "vp"),
}
