package org.idp.wallet.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.idp.wallet.verifiable_credentials_library.ui.VcHomeActivity

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LauncherView() }
    val intent = Intent(this, VcHomeActivity::class.java)
    startActivity(intent)
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
  LauncherView()
}

@Composable
fun LauncherView() {
  Column(
      modifier = Modifier.fillMaxWidth().fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
      }
}
