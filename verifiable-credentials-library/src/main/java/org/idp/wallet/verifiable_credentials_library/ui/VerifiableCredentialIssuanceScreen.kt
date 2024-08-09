package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
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
            Modifier
                .fillMaxWidth()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = Dp(16.0F),
                    end = Dp(16.0F)
                ),
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
                              Column(modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                                  verticalArrangement = Arrangement.spacedBy(Dp(8.0F))
                                  ) {
                                  Text(text = vciResult.id)
                                  Text(text = vciResult.issuer)
                                  Text(text = vciResult.transactionId ?: "")
                                  Text(text = vciResult.status.name)
                              }
                          }
                      )
                  }
                }
              }
            }
      })
}
