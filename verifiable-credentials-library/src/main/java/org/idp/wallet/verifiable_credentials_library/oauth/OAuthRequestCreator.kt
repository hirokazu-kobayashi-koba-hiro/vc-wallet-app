package org.idp.wallet.verifiable_credentials_library.oauth

import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.ResponseType
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.PresentationDefinition

class OAuthRequestCreator(private val oAuthRequestParameters: OAuthRequestParameters) {

  fun create(): OAuthRequest {
    val identifier: String = UUID.randomUUID().toString()
    val scopes: Set<String> = setOf()
    val responseType: ResponseType = ResponseType.undefined
    val clientId: String = ""
    val redirectUri: String = ""
    val state: String = ""
    val responseMode: ResponseMode = ResponseMode.undefined
    val nonce: String = ""
    val requestObject: String = ""
    val requestUri: String = ""
    val presentationDefinition: PresentationDefinition = PresentationDefinition()
    val presentationDefinitionUri: String = ""
    return OAuthRequest(
        identifier = identifier,
        scopes = scopes,
        responseType = responseType,
        clientId = clientId,
        redirectUri = redirectUri,
        state = state,
        responseMode = responseMode,
        nonce = nonce,
        requestObject = requestObject,
        requestUri = requestUri,
        presentationDefinition = presentationDefinition)
  }
}
