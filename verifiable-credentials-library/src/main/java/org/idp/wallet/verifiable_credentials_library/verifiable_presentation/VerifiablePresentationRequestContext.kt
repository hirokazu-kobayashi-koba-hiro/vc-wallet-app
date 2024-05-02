package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.type.vp.PresentationDefinition

class VerifiablePresentationRequestContext(
    val parameters: VerifiablePresentationRequestParameters,
    val authorizationRequest: AuthorizationRequest,
    val walletConfiguration: WalletConfiguration,
    val clientConfiguration: ClientConfiguration
) {
  fun getPresentationDefinition(): PresentationDefinition {
    return authorizationRequest.presentationDefinition
  }

  fun getIssuer(): String {
    return walletConfiguration.issuer
  }

  fun getRedirectUri(): String {
    authorizationRequest.redirectUri?.let {
      return it
    }
    return clientConfiguration.redirectUris[0]
  }

  fun isDirectPost(): Boolean {
    return authorizationRequest.isDirectPost()
  }
}
