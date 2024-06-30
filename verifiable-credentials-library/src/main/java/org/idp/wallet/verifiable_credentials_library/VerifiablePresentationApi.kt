package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.activity.OAuthErrorActivityWrapper
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.AuthorizationResponseCallbackService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.AuthorizationResponseCreator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationViewData
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluator

class VerifiablePresentationApi(
    val registry: VerifiableCredentialRegistry,
    private val verifiablePresentationRequestContextService:
        VerifiablePresentationRequestContextService
) {

  suspend fun handleRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Unit> {
    try {
      Log.d("VcWalletLibrary", "handleVpRequest")
      val verifiablePresentationRequestContext =
          verifiablePresentationRequestContextService.create(url)
      val records = registry.getAllAsCollection()
      val presentationDefinition = verifiablePresentationRequestContext.getPresentationDefinition()
      // create viewData
      val viewData = VerifiablePresentationViewData()
      val evaluator = PresentationDefinitionEvaluator(presentationDefinition, records)
      val evaluation = evaluator.evaluate()
      val confirmationResult = confirm(context, viewData = viewData, evaluation, interactor)
      val authorizationResponseCreator =
          AuthorizationResponseCreator(
              verifiablePresentationRequestContext = verifiablePresentationRequestContext,
              evaluation = evaluation)
      val authorizationResponse = authorizationResponseCreator.create()
      val authorizationResponseCallbackService =
          AuthorizationResponseCallbackService(
              context, verifiablePresentationRequestContext, authorizationResponse)
      authorizationResponseCallbackService.callback()

      return Result.success(Unit)
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
