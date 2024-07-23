package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentials
import org.idp.wallet.verifiable_credentials_library.ui.component.LinkButton
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials

@Preview
@Composable
fun WalletRegistrationPreView() {
  WalletRegistrationScreen(
      createCredential = { password: String ->
        return@WalletRegistrationScreen WalletCredentials(
            credentials = Credentials.create(""), bip39Wallet = Bip39Wallet("", ""))
      },
      goNext = {},
      goNextToRestore = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletRegistrationScreen(
    createCredential: (password: String) -> WalletCredentials,
    goNext: (seed: String) -> Unit,
    goNextToRestore: () -> Unit,
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  VcWalletTheme {
    Scaffold(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        topBar = {
          CenterAlignedTopAppBar(
              modifier = Modifier.fillMaxWidth().padding(),
              title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.AccountBox, contentDescription = "AccountBox")
                      Spacer(modifier = Modifier.padding(Dp(4.0F)))
                      Text(
                          text = "Wallet Registration",
                          style = MaterialTheme.typography.displayMedium)
                    }
              })
        },
        content = { innerPadding ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          top = innerPadding.calculateTopPadding(),
                          start = Dp(20.0F),
                          end = Dp(20.0F)),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(Dp(20.0F)),
              content = {
                Text(text = "Create Account", style = MaterialTheme.typography.displaySmall)
                OutlinedTextField(
                    label = { Text(text = "username") },
                    value = username,
                    onValueChange = { username = it })
                OutlinedTextField(
                    label = { Text(text = "password") },
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                      val image =
                          if (passwordVisible) Icons.Default.Visibility
                          else Icons.Default.VisibilityOff
                      IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                      }
                    },
                )
                Button(
                    content = { Text(text = "next") },
                    onClick = {
                      val walletCredentials = createCredential(password)
                      goNext(walletCredentials.bip39Wallet.mnemonic)
                    })
                LinkButton(text = "restore credential with seed?", onClick = { goNextToRestore() })
              })
        },
    )
  }
}
