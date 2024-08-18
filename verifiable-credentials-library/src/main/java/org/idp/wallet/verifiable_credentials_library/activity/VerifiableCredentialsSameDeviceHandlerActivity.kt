package org.idp.wallet.verifiable_credentials_library.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectApi
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsApi
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.DefaultVerifiableCredentialInteractor

class VerifiableCredentialsSameDeviceHandlerActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // FIXME consider case of unregistration user
    VerifiableCredentialsClient.initialize(this)
    val queries = intent.data.toString()
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
      Toast.makeText(this, throwable.message ?: "unexpected error", Toast.LENGTH_LONG).show()
    }
    lifecycleScope.launch(errorHandler) {
      val user = OpenIdConnectApi.getCurrentUser()
      VerifiableCredentialsApi.handlePreAuthorization(
          context = this@VerifiableCredentialsSameDeviceHandlerActivity,
          url = queries,
          subject = user.sub,
          interactor = DefaultVerifiableCredentialInteractor())
    }
  }
}
