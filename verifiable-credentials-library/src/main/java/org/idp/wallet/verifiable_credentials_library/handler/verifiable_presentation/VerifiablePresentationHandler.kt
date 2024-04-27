package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationViewData

class VerifiablePresentationHandler(
    val registry: VerifiableCredentialRegistry,
    private val oAuthRequestHandler: OAuthRequestHandler
) {

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): VerifiablePresentationRequestResponse {
    Log.d("Vc library", "handleVpRequest")
    val oAuthRequestContext = oAuthRequestHandler.handleRequest(url)
    // verify request
    // find vc
    val records = registry.getAllAsCollection()
    val presentationDefinition = oAuthRequestContext.getPresentationDefinition()
    val filtered =
        presentationDefinition?.let {
          return@let records.filter(it)
        } ?: records
    // create viewData
    val viewData = VerifiablePresentationViewData()
    val confirmationResult = confirm(context, viewData = viewData, filtered, interactor)
    if (!confirmationResult) {}

    return VerifiablePresentationRequestResponse(
        parameters = oAuthRequestContext.parameters,
        authorizationRequest = oAuthRequestContext.authorizationRequest,
        walletConfiguration = oAuthRequestContext.walletConfiguration,
        clientConfiguration = oAuthRequestContext.clientConfiguration,
        verifiableCredentialsRecords = filtered)
  }

  private suspend fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      filtered: VerifiableCredentialsRecords,
      interactor: VerifiablePresentationInteractor
  ): Boolean = suspendCoroutine { continuation ->
    val callbackHandler =
        object : VerifiablePresentationInteractorCallback {
          override fun accept(verifiableCredentialIds: List<String>) {
            continuation.resume(true)
          }

          override fun reject() {
            continuation.resume(false)
          }
        }
    interactor.confirm(context = context, viewData = viewData, filtered, callback = callbackHandler)
  }
}

interface VerifiablePresentationInteractor {
  fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      verifiableCredentialsRecords: VerifiableCredentialsRecords,
      callback: VerifiablePresentationInteractorCallback
  )
}

interface VerifiablePresentationInteractorCallback {
  fun accept(verifiableCredentialIds: List<String>)

  fun reject()
}
