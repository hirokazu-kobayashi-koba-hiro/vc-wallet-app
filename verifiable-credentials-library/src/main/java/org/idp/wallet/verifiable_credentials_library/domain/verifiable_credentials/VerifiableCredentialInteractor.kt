package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.activity.DefaultVcConsentActivity
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata

class DefaultVerifiableCredentialInteractor : VerifiableCredentialInteractor {
  override fun confirm(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      callback: VerifiableCredentialInteractorCallback
  ) {
    VerifiableCredentialInteracotrCallbackProvider.callback = callback
    DefaultVcConsentActivity.start(context, credentialIssuerMetadata, credentialOffer)
  }
}

interface VerifiableCredentialInteractor {
  fun confirm(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      callback: VerifiableCredentialInteractorCallback
  )
}

interface VerifiableCredentialInteractorCallback {
  fun accept(txCode: String)

  fun reject()
}

object VerifiableCredentialInteracotrCallbackProvider {
  lateinit var callback: VerifiableCredentialInteractorCallback
}
