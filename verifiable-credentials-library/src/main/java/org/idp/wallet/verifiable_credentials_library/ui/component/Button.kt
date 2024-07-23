package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun LinkButton(text: String, onClick: () -> Unit) {
  Text(
      text = text,
      modifier = Modifier.clickable { onClick() },
      color = Color.Blue,
      textDecoration = TextDecoration.Underline)
}
