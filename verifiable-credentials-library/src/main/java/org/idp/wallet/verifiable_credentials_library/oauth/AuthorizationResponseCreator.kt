package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationSubmission
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class AuthorizationResponseCreator(
    private val oAuthRequestContext: OAuthRequestContext,
    private val selectedVerifiableCredentialIds: List<String>,
    private val verifiableCredentialsRecords: VerifiableCredentialsRecords
) {

  fun create(): AuthorizationResponse {
    val issuer = oAuthRequestContext.getIssuer()
    val redirectUri = oAuthRequestContext.getRedirectUri()
    val vpToken = createVpToken()
    val presentationSubmission = PresentationSubmission()
    return AuthorizationResponse(
        issuer = issuer,
        redirectUri = redirectUri,
        vpToken = vpToken,
        presentationSubmission = presentationSubmission)
  }

  private fun createVpToken(): String {
    val selectedVerifiableCredentialsRecords =
        verifiableCredentialsRecords.find(selectedVerifiableCredentialIds)
    val jwks = oAuthRequestContext.walletConfiguration.jwks
    // FIXME
    val keyId = "vc_wallet_jwt_key"

    return ""
  }
}
