package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
fun VcCardComponent(icon: Int, title: String, detailContent: @Composable () -> Unit) {
  val visible = remember { mutableStateOf(false) }
  Card(modifier = Modifier.fillMaxWidth().padding(Dp(16.0F))) {
    Column(
        modifier = Modifier.padding(Dp(16.0F)).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dp(8.0F))) {
          Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.padding())
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "contentDescription",
                    modifier = Modifier.size(Dp(50.0F)))
                IconButton(
                    onClick = { visible.value = !visible.value },
                    content = {
                      Icon(Icons.Default.ArrowDropDown, contentDescription = "ArrowDropDown")
                    })
              }
          Text(text = title, style = MaterialTheme.typography.displayMedium)
          if (visible.value) {
            detailContent()
          }
        }
  }
}
