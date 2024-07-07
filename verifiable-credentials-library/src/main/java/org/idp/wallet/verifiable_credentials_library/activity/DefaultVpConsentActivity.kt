package org.idp.wallet.verifiable_credentials_library.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.R
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.Constraints
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.InputDescriptorDetail
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractorCallbackProvider
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationViewData
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.ui.component.CardComponent
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class DefaultVpConsentActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewDataString = intent.getStringExtra("viewData")
    val viewData =
        viewDataString?.let {
          return@let JsonUtils.read(it, VerifiablePresentationViewData::class.java)
        } ?: VerifiablePresentationViewData()
    val evaluationString = intent.getStringExtra("evaluation")
    val evaluation =
        evaluationString?.let {
          return@let JsonUtils.read(it, PresentationDefinitionEvaluation::class.java)
        } ?: PresentationDefinitionEvaluation()
    val callback = VerifiablePresentationInteractorCallbackProvider.callback
    setContent {
      DefaultVpConsentView(
          viewData = viewData,
          evaluation = evaluation,
          onAccept = {
            callback.accept()
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

@Preview
@Composable
fun DefaultVpConsentPreView() {
  DefaultVpConsentView(
      viewData = VerifiablePresentationViewData(),
      evaluation =
          PresentationDefinitionEvaluation(
              "",
              mapOf(
                  InputDescriptorDetail("", "", null, null, Constraints(null, null)) to
                      VerifiableCredentialsRecords(
                          listOf(
                              VerifiableCredentialsRecord(
                                  "1", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "2", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "3", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "4", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "5", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "6", "", "jwt", "", mapOf("key" to "test")),
                              VerifiableCredentialsRecord(
                                  "7", "", "jwt", "", mapOf("key" to "test")))),
              )),
      onAccept = {},
      onReject = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultVpConsentView(
    viewData: VerifiablePresentationViewData,
    evaluation: PresentationDefinitionEvaluation,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
  VcWalletTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth().padding(),
        topBar = {
          CenterAlignedTopAppBar(
              modifier = Modifier.fillMaxWidth().padding(),
              title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "Present Credential",
                          style = MaterialTheme.typography.displayLarge)
                    }
              })
        },
        content = { paddingValue ->
          Column(
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(paddingValue)) {
                VerifierView(viewData)
                VerifiableCredentialsView(evaluation)
              }
        },
        bottomBar = {
          Row(
              modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Button(
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White, contentColor = Color.Black),
                border = BorderStroke(Dp(1.0F), Color.Black),
                onClick = { onReject() }) {
                  Text(text = "Reject")
                }
            Button(onClick = { onAccept() }) { Text(text = "Accept") }
          }
        },
    )
  }
}

@Composable
fun VerifierView(viewData: VerifiablePresentationViewData) {
  Column(modifier = Modifier.fillMaxWidth().padding(Dp(16.0F))) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          //          Image(
          //              painter = rememberAsyncImagePainter(viewData.verifierLogoUri),
          //              contentDescription = null)
          Text(text = viewData.verifierName)
        }
    Row {
      Text(text = "credential")
      Spacer(modifier = Modifier.padding(Dp(8.0F)))
      Text(text = viewData.credentialType)
    }
    Row {
      Text(text = "purpose")
      Spacer(modifier = Modifier.padding(Dp(8.0F)))
      Text(text = viewData.purpose)
    }
  }
}

@Composable
fun VerifiableCredentialsView(evaluation: PresentationDefinitionEvaluation) {
  val cardList = mutableListOf<Pair<String, String>>()
  evaluation.results.forEach { inputDescriptorDetail, records ->
    records.forEach { cardList.add(inputDescriptorDetail.id to JsonUtils.write(it.payload)) }
  }
  LazyColumn(modifier = Modifier.fillMaxWidth()) {
    items(cardList) { (issuer, content) ->
      CardComponent(
          icon = {
            Image(
                painter = painterResource(id = R.drawable.id_card),
                contentDescription = "contentDescription",
                modifier = Modifier.size(Dp(50.0F)))
          },
          title = issuer,
          detailContent = { Text(text = content) })
    }
  }
}
