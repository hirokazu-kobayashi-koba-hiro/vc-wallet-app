package org.idp.wallet.verifiable_credentials_library.ui

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.idp.wallet.verifiable_credentials_library.domain.wallet.toHexPrivateKey
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.web3j.crypto.Credentials

@Preview
@Composable
fun WalletKeyScreenPreview() {
  WalletKeyScreen(
      credentials =
          Credentials.create(
              "74047350830398157175675103995747748871345641635203340081011490183676411226003"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletKeyScreen(credentials: Credentials) {
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val visiblePrivateKey = remember { mutableStateOf(false) }

  VcWalletTheme {
    Scaffold(
        topBar = {
          CenterAlignedTopAppBar(
              title = { Text(text = "Wallet Key", style = MaterialTheme.typography.displayMedium) })
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          top = paddingValues.calculateTopPadding(),
                          bottom = paddingValues.calculateBottomPadding())) {
                Column {
                  Text(
                      text = "address",
                      modifier = Modifier.padding(start = Dp(20.0F)),
                      style = MaterialTheme.typography.titleMedium)
                  Row(
                      modifier = Modifier.padding(Dp(20.0F)),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                      content = {
                        Text(
                            text = credentials.address,
                            modifier = Modifier.widthIn(max = Dp(0.75f * screenWidth.value)),
                            style = MaterialTheme.typography.displaySmall)
                        IconButton(
                            onClick = {
                              val clip =
                                  ClipData.newPlainText("Mnemonic Phrase", credentials.address)
                              clipboardManager.setClip(ClipEntry(clip))
                              Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            content = {
                              Icon(Icons.Default.ContentCopy, contentDescription = "ContentCopy")
                            })
                      })
                }
                Column {
                  Text(
                      text = "private key",
                      modifier = Modifier.padding(start = Dp(20.0F)),
                      style = MaterialTheme.typography.titleMedium)
                  if (visiblePrivateKey.value) {
                    Row(
                        modifier = Modifier.padding(Dp(20.0F)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                          Text(
                              text = credentials.toHexPrivateKey(),
                              modifier = Modifier.widthIn(max = Dp(0.75f * screenWidth.value)),
                              style = MaterialTheme.typography.displaySmall)
                          IconButton(
                              onClick = {
                                val clip =
                                    ClipData.newPlainText(
                                        "Mnemonic Phrase", credentials.toHexPrivateKey())
                                clipboardManager.setClip(ClipEntry(clip))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                    .show()
                                visiblePrivateKey.value = false
                              },
                              content = {
                                Icon(Icons.Default.ContentCopy, contentDescription = "ContentCopy")
                              })
                        })
                  } else {
                    Button(
                        modifier = Modifier.padding(Dp(20.0F)),
                        onClick = { visiblePrivateKey.value = true },
                        content = {
                          Text(text = "open", style = MaterialTheme.typography.bodySmall)
                        })
                  }
                }
              }
        })
  }
}
