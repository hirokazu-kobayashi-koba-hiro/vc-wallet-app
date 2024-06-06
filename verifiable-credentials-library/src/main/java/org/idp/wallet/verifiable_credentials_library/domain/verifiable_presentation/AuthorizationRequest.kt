package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.ResponseMode
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.ResponseType
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.PresentationDefinition

class AuthorizationRequest(
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
    val presentationDefinition: PresentationDefinition,
    val presentationDefinitionUri: String? = null
) {

  fun isDirectPost(): Boolean {
    responseMode?.let {
      return it == ResponseMode.direct_post
    }
    return false
  }
}
