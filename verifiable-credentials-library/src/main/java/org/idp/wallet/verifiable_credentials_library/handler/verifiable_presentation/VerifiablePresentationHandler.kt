package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import android.util.Log
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry

class VerifiablePresentationHandler(
    val registry: VerifiableCredentialRegistry,
    private val oAuthRequestHandler: OAuthRequestHandler
) {

  suspend fun handleVpRequest(url: String): VerifiablePresentationRequestResponse {
    Log.d("Vc library", "handleVpRequest")
    val oAuthRequestContext = oAuthRequestHandler.handleRequest(url)
    // verify request
    // find vc
    val records = registry.getAllAsCollection()
    val presentationDefinition = oAuthRequestContext.getPresentationDefinition()
    val filterVerifiableCredential = presentationDefinition?.filterVerifiableCredential(records)
    // create viewData
    return VerifiablePresentationRequestResponse(filterVerifiableCredential)
  }

  suspend fun issueVpToken() {
    // create id_token and vp_token and presentation_submission
    // create response
    // return response
  }
}
