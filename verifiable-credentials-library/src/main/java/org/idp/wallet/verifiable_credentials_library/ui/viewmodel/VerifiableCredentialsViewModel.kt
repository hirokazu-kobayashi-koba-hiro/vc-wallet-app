package org.idp.wallet.verifiable_credentials_library.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectApi
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.domain.openid_connect.OpenIdConnectResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
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
  val vciState = _vcContent.asStateFlow()
  val loadingState = _loading.asStateFlow()
  val systemDialogState = _systemDialogState.asStateFlow()
  val loginState = _loginState.asStateFlow()

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
      format: String,
      interactor: VerifiableCredentialInteractor
  ) {
    try {
      _loading.value = true
      VerifiableCredentialsClient.handlePreAuthorization(
          context, subject(), uri, format, interactor)
    } finally {
      _loading.value = false
    }
  }

  suspend fun getAllCredentials() {
    try {
      _loading.value = true
      val allCredentials = VerifiableCredentialsClient.getAllCredentials(subject())
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
      val result = VerifiableCredentialsClient.handleVpRequest(context, subject(), url, interactor)
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

  private fun subject(): String {
    return loginState.value.userinfoResponse?.sub ?: ""
  }
}
