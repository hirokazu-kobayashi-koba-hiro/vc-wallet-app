package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationDefinition

class OAuthRequestContext(
    val parameters: OAuthRequestParameters,
    val oAuthRequest: OAuthRequest,
    val walletConfiguration: WalletConfiguration,
    val clientConfiguration: ClientConfiguration
) {
  fun getPresentationDefinition(): PresentationDefinition? {
    return oAuthRequest.presentationDefinition
  }
}
