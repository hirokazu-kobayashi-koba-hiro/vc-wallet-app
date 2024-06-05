package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor

class VerifiableCredentialIssuanceViewModel : ViewModel() {

  var _vcContent = MutableStateFlow(mapOf<String, VerifiableCredentialsRecords>())
  var _loading = MutableStateFlow(false)
  val vciState = _vcContent.asStateFlow()
  val loadingState = _loading.asStateFlow()

  suspend fun requestVcOnPreAuthorization(
      context: Context,
      uri: String,
      format: String,
      interactor: VerifiableCredentialInteractor
  ) {
    try {
      _loading.value = true
      VerifiableCredentialsClient.handlePreAuthorization(context, uri, format, interactor)
    } finally {
      _loading.value = false
    }
  }

  fun getAllCredentials() {
    try {
      _loading.value = true
      val allCredentials = VerifiableCredentialsClient.getAllCredentials()
      _vcContent.value = allCredentials
    } finally {
      _loading.value = false
    }
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ) {
    try {
      _loading.value = true
      val result = VerifiableCredentialsClient.handleVpRequest(context, url, interactor)
      println(result)
    } finally {
      _loading.value = false
    }
  }
}
