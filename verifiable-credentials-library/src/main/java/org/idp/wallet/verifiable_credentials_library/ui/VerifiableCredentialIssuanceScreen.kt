package org.idp.wallet.verifiable_credentials_library.ui

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.R
import org.idp.wallet.verifiable_credentials_library.ui.component.CardComponent
import org.idp.wallet.verifiable_credentials_library.ui.component.LoadingScreen
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VcScreen(
    viewModel: VerifiableCredentialsViewModel,
    resolveQrCode: (format: String) -> Unit,
    issueVcWhenAuthorizationCodeFlow: (issuer: String) -> Unit
) {
  var format by remember { mutableStateOf("vc+sd-jwt") }
  var issuer by remember { mutableStateOf("https://trial.authlete.net") }
  val vciResultsState = viewModel.vciResultsState.collectAsState()
  val coroutineScope = CoroutineScope(Dispatchers.Main)
  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.findAllCredentialIssuanceResults() }

  if (viewModel.loadingState.collectAsState().value) {
    LoadingScreen()
    return
  }
  Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Issue Vc", style = MaterialTheme.typography.displayMedium)
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = Dp(16.0F),
                        end = Dp(16.0F)),
            verticalArrangement = Arrangement.spacedBy(Dp(16.0F)),
            horizontalAlignment = Alignment.Start) {
              Column(verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
                Text(text = "pre-authorization")
                Button(onClick = { resolveQrCode(format) }) { Text(text = "scan QR") }
              }
              Column(verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
                Text(text = "authorization-code")
                OutlinedTextField(value = issuer, onValueChange = { issuer = it })
                Button(onClick = { issueVcWhenAuthorizationCodeFlow(issuer) }) {
                  Text(text = "request vc")
                }
              }
              Divider()
              Column {
                Text(text = "issuance result")
                LazyColumn {
                  items(vciResultsState.value) { vciResult ->
                    CardComponent(
                        title = vciResult.credentialConfigurationId,
                        icon = {
                          Image(
                              painter = painterResource(id = R.drawable.proof),
                              contentDescription = "contentDescription",
                              modifier = Modifier.size(Dp(50.0F)))
                        },
                        detailContent = {
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Spacer(modifier = Modifier.padding())
                                Button(
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = Color.Transparent),
                                    onClick = {
                                      val errorHandler = CoroutineExceptionHandler { _, error ->
                                        Toast.makeText(
                                                context,
                                                error.message ?: "unexpected error",
                                                Toast.LENGTH_LONG)
                                            .show()
                                      }
                                      coroutineScope.launch(errorHandler) {
                                        viewModel.handleDeferredCredential(
                                            context = context,
                                            credentialIssuanceResultId = vciResult.id)
                                      }
                                    }) {
                                      Image(
                                          painter =
                                              painterResource(id = R.drawable.three_point_reader),
                                          contentDescription = "three_point_reader",
                                          modifier = Modifier.size(Dp(25.0F)))
                                    }
                              }
                          Column(
                              modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                              verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
                                RowContent(label = "id", value = vciResult.id)
                                RowContent(label = "issuer", value = vciResult.issuer)
                                RowContent(
                                    label = "transactionId", value = vciResult.transactionId ?: "")
                                RowContent(label = "status", value = vciResult.status.name)
                              }
                        })
                  }
                }
              }
            }
      })
}

@Composable
private fun RowContent(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(end = Dp(16.0F)))
    Text(text = value, style = MaterialTheme.typography.bodyMedium)
  }
}
