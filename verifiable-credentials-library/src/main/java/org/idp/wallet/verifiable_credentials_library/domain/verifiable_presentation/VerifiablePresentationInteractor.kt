package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import android.content.Context
import android.content.Intent
import org.idp.wallet.verifiable_credentials_library.activity.DefaultVpConsentActivity
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class DefaultVerifiablePresentationInteractor : VerifiablePresentationInteractor {

  override fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      evaluation: PresentationDefinitionEvaluation,
      callback: VerifiablePresentationInteractorCallback
  ) {
    VerifiablePresentationInteractorCallbackProvider.callback = callback
    val intent = Intent(context, DefaultVpConsentActivity::class.java)
    val viewDataString = JsonUtils.write(viewData)
    val evaluationString = JsonUtils.write(evaluation)
    intent.putExtra("viewData", viewDataString)
    intent.putExtra("evaluation", evaluationString)
    context.startActivity(intent)
  }
}

object VerifiablePresentationInteractorCallbackProvider {
  lateinit var callback: VerifiablePresentationInteractorCallback
}

interface VerifiablePresentationInteractor {
  fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      evaluation: PresentationDefinitionEvaluation,
      callback: VerifiablePresentationInteractorCallback
  )
}

interface VerifiablePresentationInteractorCallback {
  fun accept()

  fun reject()
}
