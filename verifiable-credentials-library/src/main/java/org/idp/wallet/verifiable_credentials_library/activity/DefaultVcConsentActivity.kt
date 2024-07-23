package org.idp.wallet.verifiable_credentials_library.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.idp.wallet.verifiable_credentials_library.R
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.BackgroundImage
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.Display
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.Logo
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteracotrCallbackProvider
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class DefaultVcConsentActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val cancelCallback = {
      VerifiableCredentialInteracotrCallbackProvider.callback.reject()
      finish()
    }
    val credentialIssuerMetadataValue =
        intent.getStringExtra("credentialIssuerMetadata") ?: throw RuntimeException("")
    val credentialIssuerMetadata =
        JsonUtils.read(
            credentialIssuerMetadataValue, CredentialIssuerMetadata::class.java, snakeCase = false)
    val credentialOfferValue =
        intent.getStringExtra("credentialOffer") ?: throw RuntimeException("")
    val credentialOffer =
        JsonUtils.read(credentialOfferValue, CredentialOffer::class.java, snakeCase = false)
    setContent {
      DefaultVcView(
          credentialIssuerMetadata = credentialIssuerMetadata,
          credentialOffer = credentialOffer,
          onContinue = {
            VerifiableCredentialInteracotrCallbackProvider.callback.accept(it)
            finish()
          },
          onCancel = { cancelCallback() })
    }
    onBackPressedDispatcher.addCallback {
      cancelCallback()
      finish()
    }
  }
}

@Preview
@Composable
fun DefaultVcPreview() {
  DefaultVcView(
      credentialIssuerMetadata =
          CredentialIssuerMetadata(
              credentialIssuer = "https://example.com",
              authorizationServers = listOf(""),
              credentialEndpoint = "",
              batchCredentialEndpoint = null,
              deferredCredentialEndpoint = null,
              notificationEndpoint = null,
              credentialResponseEncryption = null,
              credentialIdentifiersSupported = false,
              signedMetadata = null,
              display =
                  listOf(
                      Display(
                          "Example University",
                          "en-US",
                          Logo(
                              "https://university.example.edu/public/logo.png",
                              "a square logo of a university"),
                          "ExampleUniversity Degree Credential",
                          "#12107c",
                          BackgroundImage(""),
                          "#FFFFFF")),
              credentialConfigurationsSupported =
                  mapOf(
                      "UniversityDegreeCredential" to
                          CredentialConfiguration(
                              "",
                              "",
                              null,
                              null,
                              listOf(
                                  Display(
                                      "Sample University",
                                      "en-US",
                                      Logo(
                                          "https://university.example.edu/public/logo.png",
                                          "a square logo of a university"),
                                      "ExampleUniversity Degree Credential",
                                      "#12107c",
                                      BackgroundImage(""),
                                      "#FFFFFF"))))),
      credentialOffer =
          CredentialOffer(
              credentialIssuer = "https://example.com",
              credentialConfigurationIds =
                  listOf(
                      "UniversityDegreeCredential",
                      "HighSchoolDegreeCredential",
                      "JuniorHighSchoolDegreeCredential")),
      onContinue = {},
      onCancel = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultVcView(
    credentialIssuerMetadata: CredentialIssuerMetadata,
    credentialOffer: CredentialOffer,
    onContinue: (txCode: String) -> Unit,
    onCancel: () -> Unit
) {
  var txCode by remember { mutableStateOf("") }
  VcWalletTheme(darkTheme = false) {
    Scaffold(
        modifier = Modifier.fillMaxWidth().padding(top = Dp(24.0F), bottom = Dp(24.0F)),
        topBar = {
          CenterAlignedTopAppBar(
              modifier = Modifier.fillMaxWidth().padding(),
              title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.AccountCircle,
                          contentDescription = "AccountCircle")
                      Spacer(modifier = Modifier.padding(Dp(4.0F)))
                      Text(text = "Issue Credential", style = MaterialTheme.typography.displayLarge)
                    }
              })
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          top = paddingValues.calculateTopPadding(),
                          bottom = paddingValues.calculateBottomPadding(),
                          start = Dp(16.0F),
                          end = Dp(16.0F)),
              verticalArrangement = Arrangement.spacedBy(Dp(16.0F)),
              horizontalAlignment = Alignment.CenterHorizontally) {
                CredentialCards(credentialOffer, credentialIssuerMetadata)
                OutlinedTextField(
                    label = { Text(text = "Transaction Code") },
                    value = txCode,
                    onValueChange = { txCode = it })
              }
        },
        bottomBar = {
          Row(
              modifier = Modifier.fillMaxWidth().padding(Dp(20.0F)),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = onCancel,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(width = Dp(1.0F), color = Color.Black)) {
                      Text("Cancel")
                    }
                Button(
                    onClick = { onContinue(txCode) },
                    colors = ButtonDefaults.buttonColors(contentColor = Color.White)) {
                      Text("Continue")
                    }
              }
        },
    )
  }
}

@Composable
fun CredentialCards(
    credentialOffer: CredentialOffer,
    credentialIssuerMetadata: CredentialIssuerMetadata
) {
  LazyColumn(Modifier.fillMaxWidth().padding(Dp(16.0F))) {
    items(credentialOffer.credentialConfigurationIds) {
      CredentialCard(
          credential = it,
          credentialConfiguration = credentialIssuerMetadata.findCredentialConfiguration(it))
    }
  }
}

@Composable
fun CredentialCard(credential: String, credentialConfiguration: CredentialConfiguration?) {
  Card(
      modifier = Modifier.padding(top = Dp(16.0F)),
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(width = Dp(2.0F), color = Color.Black),
      content = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
            verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
              credentialConfiguration?.getFirstLogo()?.let {
                AsyncImage(
                    model = it.uri,
                    contentDescription = it.altText,
                    modifier = Modifier.height(Dp(100.0F)),
                    contentScale = ContentScale.Crop)
              }
                  ?: Image(
                      painter = painterResource(id = R.drawable.id_card),
                      contentDescription = "contentDescription",
                      modifier = Modifier.size(Dp(50.0F)))
              Row(
                  modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "type",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(end = Dp(8.0F)))
                    Text(
                        text = credentialConfiguration?.getFirstDisplay()?.name ?: credential,
                        style = MaterialTheme.typography.bodyLarge)
                  }
              credentialConfiguration?.getFirstDisplay()?.description?.let {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dp(8.0F)),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = "description",
                          style = MaterialTheme.typography.displaySmall,
                          modifier = Modifier.padding(end = Dp(8.0F)))
                      Text(text = it, style = MaterialTheme.typography.bodyLarge)
                    }
              }
            }
      })
}
