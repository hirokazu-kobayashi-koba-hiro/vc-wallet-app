package org.idp.wallet.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.ui.VcHomeActivity

class LauncherActivity : FragmentActivity() {

  private val viewModel: LauncherViewModel by lazy {
    ViewModelProvider(this).get(LauncherViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LauncherView(viewModel = viewModel, onClick = { login() }) }
    login()
  }

  private fun login() {
    lifecycleScope.launch {
      viewModel.login(
          this@LauncherActivity,
          successCallback = {
            val intent = Intent(this@LauncherActivity, VcHomeActivity::class.java)
            startActivity(intent)
          })
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
  LauncherView(viewModel = LauncherViewModel(), onClick = {})
}

@Composable
fun LauncherView(viewModel: LauncherViewModel, onClick: () -> Unit) {
  var loading = viewModel.loadingState.collectAsState()
  Column(
      modifier = Modifier.fillMaxWidth().fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (loading.value) {
          CircularProgressIndicator()
          return
        }
        Button(onClick = onClick) {
          Text(text = "start", style = MaterialTheme.typography.bodyMedium)
        }
      }
}
