package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationDefinition
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.ResponseType

class OAuthRequest(
    val identifier: String = "",
    val scopes: Set<String>? = null,
    val responseType: ResponseType? = null,
    val clientId: String = "",
    val redirectUri: String? = null,
    val state: String? = null,
    val responseMode: ResponseMode? = null,
    val nonce: String? = null,
    val requestObject: String? = null,
    val requestUri: String? = null,
    val presentationDefinition: PresentationDefinition? = null,
    val presentationDefinitionUri: String? = null
) {}
