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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
                  listOf("UniversityDegreeCredential", "HighSchoolDegreeCredential")),
      onContinue = {},
      onCancel = {})
}

@Composable
fun DefaultVcView(
    credentialIssuerMetadata: CredentialIssuerMetadata,
    credentialOffer: CredentialOffer,
    onContinue: (pinCode: String) -> Unit,
    onCancel: () -> Unit
) {
  var pinCode by remember { mutableStateOf("") }
  VcWalletTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth().padding(top = Dp(24.0F), bottom = Dp(24.0F)),
        topBar = {
          TopAppBar(
              modifier = Modifier.fillMaxWidth().padding(),
              backgroundColor = Color.Black,
              contentColor = Color.White,
              title = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Row {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "AccountCircle")
                    Spacer(modifier = Modifier.padding(Dp(4.0F)))
                    Text(text = "Credential")
                  }
                }
              })
        },
        content = { paddingValues ->
          Column(
              modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
              verticalArrangement = Arrangement.spacedBy(Dp(16.0F)),
              horizontalAlignment = Alignment.CenterHorizontally) {
                credentialOffer.credentialConfigurationIds.forEach { credential ->
                  Card(
                      backgroundColor = Color.Transparent,
                      contentColor = Color.Black,
                      border = BorderStroke(width = Dp(2.0F), color = Color.Black)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Dp(4.0F))) {
                          val credentialConfiguration =
                              credentialIssuerMetadata.findCredentialConfiguration(credential)
                          AsyncImage(
                              model = credentialConfiguration?.getFirstLogo()?.uri,
                              contentDescription = "contentDescription",
                              modifier = Modifier.size(Dp(160.0F)),
                              contentScale = ContentScale.Crop)
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Issuer", style = MaterialTheme.typography.subtitle1)
                                Text(text = "University")
                              }
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Type", style = MaterialTheme.typography.subtitle1)
                                Text(text = credential)
                              }
                          credentialConfiguration?.getFirstDisplay()?.description?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                  Text(
                                      text = "Description",
                                      style = MaterialTheme.typography.subtitle1)
                                  Text(text = it)
                                }
                          }
                        }
                      }
                }
                Text("Insert provided code for the offer from Issuer")
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "PinCode") },
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                    value = pinCode,
                    onValueChange = { pinCode = it })
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
                            backgroundColor = Color.White, contentColor = Color.Black)) {
                      Text("Cancel")
                    }
                Button(
                    onClick = { onContinue(pinCode) },
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = Color.Black, contentColor = Color.White)) {
                      Text("Continue")
                    }
              }
        },
    )
  }
}
