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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

class DefaultVerifiableCredentialConsentActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val cancelCallback = { VerifiableCredentialInteracotrCallbackProvider.callback.reject() }
    setContent {
      DefaultVcView(
          onContinue = { VerifiableCredentialInteracotrCallbackProvider.callback.accept(it) },
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
  DefaultVcView(onContinue = {}, onCancel = {})
}

@Composable
fun DefaultVcView(onContinue: (pinCode: String) -> Unit, onCancel: () -> Unit) {
  var pinCode by remember { mutableStateOf("") }
  VcWalletTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
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
                  Text(text = "Credential")
                }
              })
        },
        content = { paddingValues ->
          Column(modifier = Modifier.fillMaxWidth().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  Card(
                      modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
                      backgroundColor = Color.Transparent,
                      contentColor = Color.Black,
                      border = BorderStroke(width = Dp(2.0F), color = Color.Black)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(Dp(4.0F))) {
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Issuer")
                                Text(text = "University")
                              }
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Type")
                                Text(text = "Degree")
                              }
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(Dp(4.0F)),
                              horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Description")
                                Text(text = "Sample")
                              }
                        }
                      }
                  OutlinedTextField(
                      label = { Text(text = "PinCode") },
                      colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                      value = pinCode,
                      onValueChange = { pinCode = it })
                }
          }
        },
        bottomBar = {
          Row(
              modifier = Modifier.fillMaxWidth().padding(Dp(16.0F)),
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
