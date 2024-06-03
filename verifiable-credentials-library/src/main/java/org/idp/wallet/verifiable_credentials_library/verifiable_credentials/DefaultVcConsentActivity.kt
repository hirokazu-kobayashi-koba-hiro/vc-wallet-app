package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.type.vc.BackgroundImage
import org.idp.wallet.verifiable_credentials_library.type.vc.CredentialConfiguration
import org.idp.wallet.verifiable_credentials_library.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.type.vc.Display
import org.idp.wallet.verifiable_credentials_library.type.vc.Logo
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

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
                          "UniversityDegreeCredential",
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
                                      "UniversityDegreeCredential",
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
    onContinue: (pinCode: String) -> Unit,
    onCancel: () -> Unit
) {
  var pinCode by remember { mutableStateOf("") }
  VcWalletTheme(darkTheme = false) {
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dp(24.0F), bottom = Dp(24.0F)),
        topBar = {
          CenterAlignedTopAppBar(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(),
              title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.AccountCircle,
                          contentDescription = "AccountCircle")
                      Spacer(modifier = Modifier.padding(Dp(4.0F)))
                      Text(text = "Credential", style = MaterialTheme.typography.displayLarge)
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
                      bottom = paddingValues.calculateBottomPadding(),
                      start = Dp(16.0F),
                      end = Dp(16.0F)
                  ),
              verticalArrangement = Arrangement.spacedBy(Dp(16.0F)),
              horizontalAlignment = Alignment.CenterHorizontally) {
                CredentialCards(credentialOffer, credentialIssuerMetadata)
                Text("Insert provided code for the offer from Issuer")
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "PinCode") },
                    value = pinCode,
                    onValueChange = { pinCode = it })
              }
        },
        bottomBar = {
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(Dp(20.0F)),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(width = Dp(1.0F), color = Color.Black)
                ) {
                  Text("Cancel")
                }
                Button(
                    onClick = { onContinue(pinCode) },
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
  LazyColumn(
      Modifier
          .fillMaxWidth()
          .padding(Dp(16.0F))) {
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
      border = BorderStroke(width = Dp(2.0F), color = Color.Black)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(4.0F))) {
          AsyncImage(
              model =
                  credentialConfiguration?.getFirstLogo()?.uri,
              contentDescription = credentialConfiguration?.getFirstLogo()?.altText,
              modifier = Modifier.height(Dp(100.0F)),
              contentScale = ContentScale.Crop)
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(Dp(4.0F)),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Issuer", style = MaterialTheme.typography.displaySmall)
                Text(text = "University", style = MaterialTheme.typography.bodyLarge)
              }
          Row(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(Dp(4.0F)),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Type", style = MaterialTheme.typography.displaySmall)
                Text(text = credential, style = MaterialTheme.typography.bodyLarge)
              }
          credentialConfiguration?.getFirstDisplay()?.description?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dp(4.0F)),
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(text = "Description", style = MaterialTheme.typography.displaySmall)
                  Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }
          }
        }
      }
}
