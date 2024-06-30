package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

@Preview
@Composable
fun WalletSeedConfirmationPreviewView() {
  WalletSeedConfirmationView("seed test registration car bug transaction", {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSeedConfirmationView(seed: String, goNext: () -> Unit) {

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
                          style = MaterialTheme.typography.displayLarge)
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
                Text(text = "Confirm Seed", style = MaterialTheme.typography.displayMedium)
                Text(text = seed, style = MaterialTheme.typography.displaySmall)
                Button(content = { Text(text = "next") }, onClick = { goNext() })
              })
        },
    )
  }
}
