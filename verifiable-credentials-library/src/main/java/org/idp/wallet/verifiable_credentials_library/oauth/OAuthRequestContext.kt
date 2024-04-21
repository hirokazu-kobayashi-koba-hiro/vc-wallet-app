package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfiguration

class OAuthRequestContext(
    val parameters: OAuthRequestParameters,
    val walletConfiguration: WalletConfiguration,
    val clientConfiguration: ClientConfiguration
) {}
