package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class DefaultVpConsentActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewDataString = intent.getStringExtra("viewData")
    val viewData =
        viewDataString?.let {
          return@let JsonUtils.read(it, VerifiablePresentationViewData::class.java)
        } ?: VerifiablePresentationViewData()
    val recordsString = intent.getStringExtra("verifiableCredentialsRecords")
    val records =
        recordsString?.let {
          return@let JsonUtils.read(it, VerifiableCredentialsRecords::class.java)
        } ?: VerifiableCredentialsRecords()
    val callback = VerifiablePresentationInteractorCallbackProvider.callback
    setContent {
      DefaultVpConsentView(
          viewData = viewData,
          records = records,
          onAccept = { selected ->
            callback.accept(selected)
            finish()
          },
          onReject = {
            callback.reject()
            finish()
          })
    }
    onBackPressedDispatcher.addCallback {
      callback.reject()
      finish()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultVpConsentView(
    viewData: VerifiablePresentationViewData,
    records: VerifiableCredentialsRecords,
    onAccept: (List<String>) -> Unit,
    onReject: () -> Unit
) {
  Scaffold(
      topBar = {
        Column(modifier = Modifier.padding(top = Dp(16.0F))) {
          TopAppBar(
              title = { Text(text = "Verifiable Presentation Consent") },
          )
        }
      },
      content = { paddingValue ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = paddingValue.calculateTopPadding())) {
              VerifierView(viewData)
              VerifiableCredentialsView(records)
            }
      },
      bottomBar = {
        Row(
            modifier = Modifier.padding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(onClick = { onReject() }) { Text(text = "reject") }
          Button(onClick = { onAccept(listOf("1")) }) { Text(text = "accept") }
        }
      },
  )
}

@Composable fun VerifierView(viewData: VerifiablePresentationViewData) {}

@Composable
fun VerifiableCredentialsView(verifiableCredentialsRecords: VerifiableCredentialsRecords) {
  val cardList = mutableListOf<Pair<String, String>>()
  verifiableCredentialsRecords.forEach { cardList.add(it.id to JsonUtils.write(it.payload)) }
  LazyColumn(modifier = Modifier.fillMaxWidth()) {
    items(cardList) { (issuer, content) -> VcCardComponent(title = issuer, content = content) }
  }
}

@Composable
fun VcCardComponent(title: String, content: String) {
  Card(modifier = Modifier.fillMaxWidth().padding(Dp(16.0F))) {
    Column(modifier = Modifier.padding(Dp(16.0F))) {
      Text(text = title, style = MaterialTheme.typography.bodyMedium)
      Spacer(modifier = Modifier.height(Dp(8.0F)))
      Text(text = content, style = MaterialTheme.typography.bodySmall)
    }
  }
}
