package org.idp.wallet.verifiable_credentials_library.oauth

import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
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
    val header = mapOf<String, Any>("kid" to keyId, "type" to "JWT")
    val payload = mutableMapOf<String, Any>()
    payload.put("id", UUID.randomUUID().toString())
    payload.put("type", listOf("VerifiablePresentation"))
    payload.put("verifiableCredential", selectedVerifiableCredentialsRecords.rawVcList())
    payload.put(
        "@context",
        listOf(
            "https://www.w3.org/ns/credentials/v2",
            "https://www.w3.org/ns/credentials/examples/v2"))
    return JoseHandler.sign(header = header, payload = payload, jwks = jwks, keyId = keyId)
  }
}
