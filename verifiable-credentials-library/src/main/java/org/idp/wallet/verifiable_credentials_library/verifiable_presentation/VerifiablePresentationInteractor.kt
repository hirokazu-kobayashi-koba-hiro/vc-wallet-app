package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import android.content.Context
import android.content.Intent
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class DefaultVerifiablePresentationInteractorExexution : VerifiablePresentationInteractor {

  override fun confirm(
      context: Context,
      viewData: VerifiablePresentationViewData,
      verifiableCredentialsRecords: VerifiableCredentialsRecords,
      callback: VerifiablePresentationInteractorCallback
  ) {
    VerifiablePresentationInteractorCallbackProvider.callback = callback
    val intent = Intent(context, DefaultVpConsentActivity::class.java)
    val viewDataString = JsonUtils.write(viewData)
    val verifiableCredentialsRecordsString = JsonUtils.write(verifiableCredentialsRecords)
    intent.putExtra("viewData", viewDataString)
    intent.putExtra("verifiableCredentialsRecords", verifiableCredentialsRecordsString)
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
      verifiableCredentialsRecords: VerifiableCredentialsRecords,
      callback: VerifiablePresentationInteractorCallback
  )
}

interface VerifiablePresentationInteractorCallback {
  fun accept(verifiableCredentialIds: List<String>)

  fun reject()
}
