package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.idp.wallet.verifiable_credentials_library.activity.OAuthErrorActivityWrapper
import org.idp.wallet.verifiable_credentials_library.domain.error.VerifiableCredentialsError
import org.idp.wallet.verifiable_credentials_library.domain.error.toVerifiableCredentialsError
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.AuthorizationResponseCallbackService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.AuthorizationResponseCreator
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationViewData
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluator

/**
 * This singleton object provides an API to handle verifiable presentation requests. It interacts
 * with the verifiable credentials and presentation services to manage presentation requests,
 * evaluations, and responses.
 */
object VerifiablePresentationApi {

  private lateinit var verifiableCredentialsService: VerifiableCredentialsService
  private lateinit var verifiablePresentationService: VerifiablePresentationService

  /**
   * Initializes the VerifiablePresentationApi with the necessary services.
   *
   * @param verifiableCredentialsService the service for handling verifiable credentials
   * @param verifiablePresentationService the service for handling verifiable presentations
   */
  internal fun initialize(
      verifiableCredentialsService: VerifiableCredentialsService,
      verifiablePresentationService: VerifiablePresentationService
  ) {
    this.verifiableCredentialsService = verifiableCredentialsService
    this.verifiablePresentationService = verifiablePresentationService
  }

  /**
   * Handles the verifiable presentation (VP) request process.
   *
   * This method creates the verifiable presentation request context, retrieves the relevant
   * credentials, evaluates the presentation definition, and manages the user confirmation process.
   * It then creates and sends the authorization response based on the evaluation.
   *
   * @param context the application context
   * @param subject the subject identifier for whom the credentials are presented
   * @param url the URL of the VP request
   * @param interactor the interactor for handling user interaction during the VP request process
   * @return a result indicating the success or failure of the operation
   */
  suspend fun handleVpRequest(
      context: Context,
      subject: String,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): VerifiableCredentialResult<Unit, VerifiableCredentialsError> {
    try {
      Log.d("VcWalletLibrary", "handleVpRequest")
      val verifiablePresentationRequestContext = verifiablePresentationService.create(url)
      val records = verifiableCredentialsService.findCredentials(subject)
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

      return VerifiableCredentialResult.Success(Unit)
    } catch (e: Exception) {

      OAuthErrorActivityWrapper.launch(context)
      val error = e.toVerifiableCredentialsError()
      return VerifiableCredentialResult.Failure(error)
    }
  }

  /**
   * Confirms the verifiable presentation request with the user.
   *
   * This method handles the user confirmation or rejection of the presentation request based on the
   * evaluation of the presentation definition. It uses a callback mechanism to resume the coroutine
   * once the user has made a decision.
   *
   * @param context the application context
   * @param viewData the view data for displaying the presentation request to the user
   * @param evaluation the result of the presentation definition evaluation
   * @param interactor the interactor handling the user interaction
   * @return a boolean indicating whether the user accepted or rejected the presentation request
   */
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
