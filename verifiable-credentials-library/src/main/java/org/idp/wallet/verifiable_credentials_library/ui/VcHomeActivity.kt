package org.idp.wallet.verifiable_credentials_library.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.DefaultVerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.DefaultVerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.component.VcCardComponent
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.viewmodel.VerifiableCredentialIssuanceViewModel

class VcHomeActivity : ComponentActivity() {

  var format: String = ""

  private val viewModel: VerifiableCredentialIssuanceViewModel by lazy {
    ViewModelProvider(this).get(VerifiableCredentialIssuanceViewModel::class.java)
  }

  private val launcher =
      this.registerForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult(),
          callback = {
            val result: IntentResult = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

            val barcodeValue = result.contents

            if (null == barcodeValue) {
              Toast.makeText(this, "Read Error", Toast.LENGTH_LONG).show()
              return@registerForActivityResult
            }
            val errorHandler = CoroutineExceptionHandler { _, error ->
              Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            }
            lifecycleScope.launch(errorHandler) {
              if (format == "vp") {
                viewModel.handleVpRequest(
                    this@VcHomeActivity,
                    barcodeValue,
                    interactor = DefaultVerifiablePresentationInteractor())
                Toast.makeText(this@VcHomeActivity, "Success", Toast.LENGTH_LONG).show()
                return@launch
              }
              viewModel.requestVcOnPreAuthorization(
                  this@VcHomeActivity,
                  barcodeValue,
                  format,
                  DefaultVerifiableCredentialInteractor())
              Toast.makeText(this@VcHomeActivity, "Success", Toast.LENGTH_LONG).show()
            }
          })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    VerifiableCredentialsClient.initialize(this, "218232426")
    setContent {
      HomeView(
          viewModel,
          onClick = {
            format = it
            val intent = Intent(this@VcHomeActivity, PortraitCaptureActivity::class.java)
            intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "Scan a QR code")
            launcher.launch(intent)
          },
          onClickShow = { viewModel.getAllCredentials() })
    }
    lifecycleScope.launch { viewModel.getAllCredentials() }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
  HomeView(viewModel = VerifiableCredentialIssuanceViewModel(), onClick = {}, onClickShow = {})
}

@Composable
fun HomeView(
    viewModel: VerifiableCredentialIssuanceViewModel,
    onClick: (format: String) -> Unit,
    onClickShow: () -> Unit
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
        }) {
          when (currentDestination) {
            AppDestinations.HOME ->
                HomeScreen(viewModel = viewModel, onClick = onClick, onClickShow = onClickShow)
            AppDestinations.VC ->
                VcScreen(viewModel = viewModel, onClick = onClick, onClickShow = onClickShow)
            AppDestinations.VP -> VpScreen(viewModel = viewModel, onClick = onClick)
          }
        }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VerifiableCredentialIssuanceViewModel,
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
            Log.d("Vc library app", issuer)
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VcScreen(
    viewModel: VerifiableCredentialIssuanceViewModel,
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
    viewModel: VerifiableCredentialIssuanceViewModel,
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
