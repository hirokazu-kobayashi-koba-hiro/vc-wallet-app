package org.idp.wallet.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import java.util.UUID
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.activity.VerifiableCredentialsActivity
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest

class LauncherActivity : FragmentActivity() {

  private val viewModel: LauncherViewModel by lazy {
    ViewModelProvider(this).get(LauncherViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { LauncherView(viewModel = viewModel, onClick = { login() }) }
    val request =
        OpenIdConnectRequest(
            issuer = "https://dev-l6ns7qgdx81yv2rs.us.auth0.com",
            clientId = "sKUsWLY5BCzdXAggk78km7kOjfQP1rWR",
            scope = "openid profile phone email address offline_access",
            state = UUID.randomUUID().toString(),
            nonce = UUID.randomUUID().toString(),
            redirectUri =
                "org.idp.verifiable.credentials://dev-l6ns7qgdx81yv2rs.us.auth0.com/android/org.idp.wallet.app/callback")
    VerifiableCredentialsActivity.start(context = this, request = request, forceLogin = false)
  }

  private fun login() {
    val errorHandler =
        CoroutineExceptionHandler(
            handler = { _, throwable ->
              Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
            })
    lifecycleScope.launch(errorHandler) {
      viewModel.loginWithOpenIdConnect(
          this@LauncherActivity,
          successCallback = {
            val intent = Intent(this@LauncherActivity, VerifiableCredentialsActivity::class.java)
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
