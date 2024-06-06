package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VcCardComponent(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(Dp(16.0F))) {
        Column(modifier = Modifier.padding(Dp(16.0F))) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(Dp(8.0F)))
            Text(text = content, style = MaterialTheme.typography.bodySmall)
        }
    }
}