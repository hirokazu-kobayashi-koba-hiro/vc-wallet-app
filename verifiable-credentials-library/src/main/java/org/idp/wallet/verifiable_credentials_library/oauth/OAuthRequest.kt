package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.ResponseType
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.PresentationDefinition

class OAuthRequest(
    val identifier: String = "",
    val scopes: Set<String> = setOf(),
    val responseType: ResponseType = ResponseType.undefined,
    val clientId: String = "",
    val redirectUri: String = "",
    val state: String = "",
    val responseMode: ResponseMode = ResponseMode.undefined,
    val nonce: String = "",
    val requestObject: String = "",
    val requestUri: String = "",
    val presentationDefinition: PresentationDefinition = PresentationDefinition(),
    val presentationDefinitionUri: String = ""
) {}
