package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

@Preview
@Composable
fun LoadingScreenPreview() {
  LoadingScreen()
}

@Composable
fun LoadingScreen() {
  VcWalletTheme {
      Box(
          modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(),
          contentAlignment = Alignment.Center,
      ) {
          CircularProgressIndicator()
      }
  }
}
