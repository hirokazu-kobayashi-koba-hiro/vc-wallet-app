package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.CredentialIssuerMetadata
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractorCallback
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifiableCredentialsApiTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().getContext()
    VerifiableCredentialsClient.initialize(context, WalletConfiguration("vc-test"))
  }

  @Test
  fun handle_pre_authorization_request() {
    runBlocking {
      val intaractor =
          object : VerifiableCredentialInteractor {
            override fun confirm(
                context: Context,
                credentialIssuerMetadata: CredentialIssuerMetadata,
                credentialOffer: CredentialOffer,
                callback: VerifiableCredentialInteractorCallback
            ) {
              callback.accept(null)
            }
          }
      val url =
          "openid-credential-offer://?credential_offer_uri=https://trial.authlete.net/api/offer/8ypwDqDryNh2AjYwvN0bfv_C_zk2EGGa3hwPqONSg9Q"
      val result = VerifiableCredentialsApi.handlePreAuthorization(context, "1", url, intaractor)

      println(result)
    }
  }
}
