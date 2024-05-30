package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.type.vc.CredentialIssuerMetadata

class DefaultVerifiableCredentialInteractor : VerifiableCredentialInteractor {
  override fun confirm(
      context: Context,
      coredentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer
  ) {}
}

interface VerifiableCredentialInteractor {
  fun confirm(
      context: Context,
      coredentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer
  )
}

interface VerifiableCredentialInteractorCallback {
  fun accept(pinCode: String)

  fun reject()
}

object VerifiableCredentialInteracotrCallbackProvider {
  lateinit var callback: VerifiableCredentialInteractorCallback
}
