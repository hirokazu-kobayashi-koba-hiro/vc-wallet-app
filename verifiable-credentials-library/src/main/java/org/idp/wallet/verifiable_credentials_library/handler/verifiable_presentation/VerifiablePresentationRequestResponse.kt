package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.oauth.AuthorizationRequest
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestParameters
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class VerifiablePresentationRequestResponse(
    val parameters: OAuthRequestParameters,
    val authorizationRequest: AuthorizationRequest,
    val walletConfiguration: WalletConfiguration,
    val clientConfiguration: ClientConfiguration,
    val verifiableCredentialsRecords: VerifiableCredentialsRecords?
) {}
