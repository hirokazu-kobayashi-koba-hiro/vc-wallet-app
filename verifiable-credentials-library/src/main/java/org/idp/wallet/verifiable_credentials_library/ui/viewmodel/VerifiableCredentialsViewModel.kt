package org.idp.wallet.verifiable_credentials_library.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectApi
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialResult
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsApi
import org.idp.wallet.verifiable_credentials_library.VerifiablePresentationApi
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.OpenIdConnectResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResult
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentials
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentialsManager
import org.web3j.crypto.Credentials

class VerifiableCredentialsViewModel(
    private val walletCredentialsManager: WalletCredentialsManager
) : ViewModel() {

  private var _vcContent = MutableStateFlow(mapOf<String, VerifiableCredentialsRecords>())
  private var _loading = MutableStateFlow(false)
  private var _systemDialogState = MutableStateFlow(SystemDialogState())
  private var _loginState =
      MutableStateFlow(OpenIdConnectResponse(TokenResponse("", "", "", 0, "", "")))
  private var _vciResults = MutableStateFlow(listOf<CredentialIssuanceResult>())
  val vciState = _vcContent.asStateFlow()
  val loadingState = _loading.asStateFlow()
  val systemDialogState = _systemDialogState.asStateFlow()
  val loginState = _loginState.asStateFlow()
  val vciResultsState = _vciResults.asStateFlow()

  suspend fun login(
      context: Context,
      request: OpenIdConnectRequest,
      force: Boolean = false
  ): OpenIdConnectResponse? {
    try {
      _loading.value = true
      val openIdConnectResponse = OpenIdConnectApi.login(context, request, force)
      _loginState.value = openIdConnectResponse
      return openIdConnectResponse
    } catch (e: Exception) {
      return null
    } finally {
      _loading.value = false
    }
  }

  fun createCredential(password: String): WalletCredentials {
    val walletCredentials = walletCredentialsManager.create(subject(), password)
    return walletCredentials
  }

  fun findCredential(): Credentials? {
    return walletCredentialsManager.find(subject())
  }

  fun deleteCredential() {
    walletCredentialsManager.delete(subject())
  }

  suspend fun requestVcOnPreAuthorization(
      context: Context,
      uri: String,
      interactor: VerifiableCredentialInteractor
  ) {
    try {
      _loading.value = true
      val result =
          VerifiableCredentialsApi.handlePreAuthorization(context, subject(), uri, interactor)
      when (result) {
        is VerifiableCredentialResult.Success -> {}
        is VerifiableCredentialResult.Failure -> throw RuntimeException(result.error.description())
      }
    } finally {
      _loading.value = false
    }
  }

  suspend fun requestVcOnAuthorizationCode(
      context: Context,
      issuer: String,
      credentialConfigurationId: String,
  ) {
    try {
      _loading.value = true
      val result =
          VerifiableCredentialsApi.handleAuthorizationCode(
              context, subject(), issuer, credentialConfigurationId)
      when (result) {
        is VerifiableCredentialResult.Success -> {}
        is VerifiableCredentialResult.Failure -> throw RuntimeException(result.error.description())
      }
    } finally {
      _loading.value = false
    }
  }

  suspend fun getAllCredentials() {
    try {
      _loading.value = true
      val result = VerifiableCredentialsApi.findCredentials(subject())
      when (result) {
        is VerifiableCredentialResult.Success -> {
          _vcContent.value = result.data
        }
        is VerifiableCredentialResult.Failure -> throw RuntimeException(result.error.description())
      }
    } finally {
      _loading.value = false
    }
  }

  suspend fun findAllCredentialIssuanceResults() {
    val result = VerifiableCredentialsApi.findCredentialIssuanceResults(subject())
    when (result) {
      is VerifiableCredentialResult.Success -> {
        _vciResults.value = result.data
      }
      is VerifiableCredentialResult.Failure -> throw RuntimeException(result.error.description())
    }
  }

  suspend fun handleDeferredCredential(context: Context, credentialIssuanceResultId: String) {
    VerifiableCredentialsApi.handleDeferredCredential(
        context, subject(), credentialIssuanceResultId)
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ) {
    try {
      _loading.value = true
      val result = VerifiablePresentationApi.handleVpRequest(context, subject(), url, interactor)
      println(result)
    } finally {
      _loading.value = false
    }
  }

  fun showDialog(
      title: String,
      message: String,
      onClickPositiveButton: () -> Unit = {},
      onClickNegativeButton: () -> Unit = {}
  ) {
    _systemDialogState.value =
        SystemDialogState(
            visible = true,
            title = title,
            message = message,
            onClickPositiveButton = {
              onClickPositiveButton()
              _systemDialogState.value =
                  SystemDialogState(visible = false, title = "", message = "")
            },
            onClickNegativeButton = {
              onClickNegativeButton()
              _systemDialogState.value =
                  SystemDialogState(visible = false, title = "", message = "")
            })
  }

  fun restoreCredential(password: String, seed: String): WalletCredentials {
    return walletCredentialsManager.restore(subject(), password, seed)
  }

  private fun subject(): String {
    return loginState.value.userinfoResponse?.sub ?: ""
  }
}
