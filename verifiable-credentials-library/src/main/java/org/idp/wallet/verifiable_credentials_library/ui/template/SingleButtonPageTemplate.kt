package org.idp.wallet.verifiable_credentials_library.ui.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme

@Preview
@Composable
fun SingleButtonPagePreview() {
  SingleButtonPage(
      title = "title", content = { Text(text = "content") }, buttonLabel = "close", onClick = {})
}

@Composable
fun SingleButtonPage(
    title: String,
    content: @Composable () -> Unit,
    buttonLabel: String,
    onClick: () -> Unit
) {
  VcWalletTheme {
    Scaffold(
        topBar = {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          Dp(20.0F),
                      )) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
              }
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .fillMaxHeight()
                      .padding(
                          top = paddingValues.calculateTopPadding(),
                          start = Dp(20.0F),
                          end = Dp(20.0F),
                          bottom = Dp(20.0F)),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween) {
                content()
                Button(
                    onClick = onClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.White,
                        ),
                    border = BorderStroke(Dp(1.0F), Color.Black),
                ) {
                  Text(text = buttonLabel)
                }
              }
        })
  }
}
