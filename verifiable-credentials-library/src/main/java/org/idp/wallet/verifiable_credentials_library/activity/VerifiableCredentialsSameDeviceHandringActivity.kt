package org.idp.wallet.verifiable_credentials_library.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectApi
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsApi
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.DefaultVerifiableCredentialInteractor

class VerifiableCredentialsSameDeviceHandringActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val queries = intent.data.toString()
    lifecycleScope.launch {
      val user = OpenIdConnectApi.getCurrentUser()
      VerifiableCredentialsApi.handlePreAuthorization(
          context = this@VerifiableCredentialsSameDeviceHandringActivity,
          url = queries,
          subject = user.sub,
          interactor = DefaultVerifiableCredentialInteractor())
    }
  }
}
