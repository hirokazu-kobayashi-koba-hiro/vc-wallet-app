package org.idp.wallet.app

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor

class VerifiableCredentialIssuanceViewModel : ViewModel() {

  var _vcContent = MutableStateFlow(mapOf<String, VerifiableCredentialsRecords>())
  val vciState = _vcContent.asStateFlow()

  suspend fun request(uri: String, format: String) {
    VerifiableCredentialsClient.requestVCI(uri, format)
  }

  fun getAllCredentials() {
    val allCredentials = VerifiableCredentialsClient.getAllCredentials()
    _vcContent.value = allCredentials
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ) {
    val result = VerifiableCredentialsClient.handleVpRequest(context, url, interactor)
    println(result)
  }
}
