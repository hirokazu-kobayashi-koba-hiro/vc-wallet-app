package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import android.content.Context
import android.content.Intent
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.type.vc.CredentialIssuerMetadata

class DefaultVerifiableCredentialInteractor : VerifiableCredentialInteractor {
  override fun confirm(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer: CredentialOffer,
      callback: VerifiableCredentialInteractorCallback
  ) {
    VerifiableCredentialInteracotrCallbackProvider.callback = callback
    val intent = Intent(context, DefaultVcConsentActivity::class.java)
    intent.putExtra("credentialIssuerMetadata", JsonUtils.write(credentialIssuerMetadata))
    intent.putExtra("credentialOffer", JsonUtils.write(credentialOffer))
    context.startActivity(intent)
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
  fun accept(pinCode: String)

  fun reject()
}

object VerifiableCredentialInteracotrCallbackProvider {
  lateinit var callback: VerifiableCredentialInteractorCallback
}
