package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.basic.activity.move
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationResponseCreator
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractorCallback
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
    val authorizationResponseCreator =
        AuthorizationResponseCreator(
            oAuthRequestContext = oAuthRequestContext,
            selectedVerifiableCredentialIds = listOf("1"),
            verifiableCredentialsRecords = filtered,
        )
    val authorizationResponse = authorizationResponseCreator.create()
    if (oAuthRequestContext.isDirectPost()) {
      // TODO
      println("isDirectPost")
    }

    move(context, authorizationResponse.redirectUriValue())

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
  ): Map<String, Any> = suspendCoroutine { continuation ->
    val callbackHandler =
        object : VerifiablePresentationInteractorCallback {
          override fun accept(verifiableCredentialIds: List<String>) {
            continuation.resume(mapOf("result" to true, "selectedIds" to verifiableCredentialIds))
          }

          override fun reject() {
            continuation.resume(mapOf("result" to false))
          }
        }
    interactor.confirm(context = context, viewData = viewData, filtered, callback = callbackHandler)
  }
}
