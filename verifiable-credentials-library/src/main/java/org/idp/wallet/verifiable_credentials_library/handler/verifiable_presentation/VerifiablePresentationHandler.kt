package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationResponseCallbackService
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationResponseCreator
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthErrorActivityWrapper
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractorCallback
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationViewData

class VerifiablePresentationHandler(
    val registry: VerifiableCredentialRegistry,
    private val oAuthRequestHandler: OAuthRequestHandler
) {

  suspend fun handleRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Any> {
    try {
      Log.d("Vc library", "handleVpRequest")
      val oAuthRequestContext = oAuthRequestHandler.handleRequest(url)
      // verify request
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
      val authorizationResponseCallbackService =
          AuthorizationResponseCallbackService(context, oAuthRequestContext, authorizationResponse)
      authorizationResponseCallbackService.callback()

      return Result.success(
          VerifiablePresentationRequestResponse(
              parameters = oAuthRequestContext.parameters,
              authorizationRequest = oAuthRequestContext.authorizationRequest,
              walletConfiguration = oAuthRequestContext.walletConfiguration,
              clientConfiguration = oAuthRequestContext.clientConfiguration,
              verifiableCredentialsRecords = filtered))
    } catch (e: Exception) {
      OAuthErrorActivityWrapper.launch(context)
      return Result.failure(e)
    }
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
