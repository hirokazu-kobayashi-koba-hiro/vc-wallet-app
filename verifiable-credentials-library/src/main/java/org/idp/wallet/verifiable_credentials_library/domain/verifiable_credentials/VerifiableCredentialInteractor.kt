package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import android.content.Context
import android.content.Intent
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.ui.DefaultVcConsentActivity
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class DefaultVerifiableCredentialInteractor : VerifiableCredentialInteractor {
  override fun confirm(
      context: Context,
      credentialIssuerMetadata: CredentialIssuerMetadata,
      credentialOffer:
          org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer,
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
      credentialOffer:
          org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer,
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
