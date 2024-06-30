package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.SystemDialogState

@Composable
fun SystemDialog(systemDialogState: SystemDialogState) {
  if (systemDialogState.visible) {
    AlertDialog(
        onDismissRequest = { systemDialogState.onClickNegativeButton() },
        confirmButton = {
          TextButton(onClick = { systemDialogState.onClickPositiveButton() }) { Text("OK") }
        },
        title = { Text(text = systemDialogState.title) },
        text = { Text(text = systemDialogState.message) })
  }
}
