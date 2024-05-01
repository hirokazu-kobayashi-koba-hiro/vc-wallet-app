package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationResponseCallbackService
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationResponseCreator
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthErrorActivityWrapper
import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationDefinitionEvaluator
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
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
      // create viewData
      val viewData = VerifiablePresentationViewData()
      val evaluator = PresentationDefinitionEvaluator(presentationDefinition, records)
      val evaluation = evaluator.evaluate()
      val confirmationResult = confirm(context, viewData = viewData, evaluation, interactor)
      val authorizationResponseCreator =
          AuthorizationResponseCreator(
              oAuthRequestContext = oAuthRequestContext, evaluation = evaluation)
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
              verifiableCredentialsRecords = records))
    } catch (e: Exception) {
      OAuthErrorActivityWrapper.launch(context)
      return Result.failure(e)
    }
  }

  private suspend fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      evaluation: PresentationDefinitionEvaluation,
      interactor: VerifiablePresentationInteractor
  ): Boolean = suspendCoroutine { continuation ->
    val callbackHandler =
        object : VerifiablePresentationInteractorCallback {
          override fun accept() {
            continuation.resume(true)
          }

          override fun reject() {
            continuation.resume(false)
          }
        }
    interactor.confirm(
        context = context, viewData = viewData, evaluation, callback = callbackHandler)
  }
}
