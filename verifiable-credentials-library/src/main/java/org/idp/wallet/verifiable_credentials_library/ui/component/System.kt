package org.idp.wallet.verifiable_credentials_library.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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

@Composable
fun FloatingView(
    visible: Boolean,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit,
    start: Dp = Dp(0F),
    end: Dp = Dp(0F),
    top: Dp = Dp(0F),
    bottom: Dp = Dp(0F)
) {
  AnimatedVisibility(
      visible = visible,
      enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
      exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
      content = {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }),
            contentAlignment = Alignment.TopEnd,
        ) {
          Surface(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = start, end = end, top = top, bottom = bottom)
                      .background(color = Color.Transparent)
                      .height(IntrinsicSize.Min),
              shape = RoundedCornerShape(Dp(8.0F)),
          ) {
            Column(
                modifier = Modifier.padding(Dp(16.0F)).background(color = Color.White),
            ) {
              content()
            }
          }
        }
      })
}
