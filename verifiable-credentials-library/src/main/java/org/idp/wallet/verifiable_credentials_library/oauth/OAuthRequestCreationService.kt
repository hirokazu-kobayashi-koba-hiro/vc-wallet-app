package org.idp.wallet.verifiable_credentials_library.oauth

import java.util.UUID
import java.util.stream.Collectors
import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.basic.jose.JwtObject
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationDefinition
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.ResponseType

class OAuthRequestCreationService(private val parameters: OAuthRequestParameters) {

  // TODO requestObject pattern
  suspend fun create(): OAuthRequest {
    val identifier = UUID.randomUUID().toString()
    val jwtObject = getRequestObject()
    val scopes =
        jwtObject?.let {
          if (it.containsKey("scope")) {
            return@let it.valueAsStringFromPayload("scope")
                .split(" ")
                .stream()
                .collect(Collectors.toSet())
          }
          return@let null
        } ?: parameters.getScope()
    val responseType =
        jwtObject?.let {
          if (it.containsKey("response_type")) {
            return@let ResponseType.of(it.valueAsStringFromPayload("response_type"))
          }
          return@let null
        } ?: parameters.getResponseType()
    val clientId =
        jwtObject?.let {
          if (it.containsKey("client_id")) {
            return@let it.valueAsStringFromPayload("client_id")
          }
          return@let null
        } ?: parameters.getClientId()
    val redirectUri =
        jwtObject?.let {
          if (it.containsKey("redirect_uri")) {
            return@let it.valueAsStringFromPayload("redirect_uri")
          }
          return@let null
        } ?: parameters.getRedirectUri()
    val state =
        jwtObject?.let {
          if (it.containsKey("state")) {
            return@let it.valueAsStringFromPayload("state")
          }
          return@let null
        } ?: parameters.getState()
    val responseMode =
        jwtObject?.let {
          if (it.containsKey("response_mode")) {
            return@let ResponseMode.of(it.valueAsStringFromPayload("response_mode"))
          }
          return@let null
        } ?: parameters.getResponseMode()
    val nonce =
        jwtObject?.let {
          if (it.containsKey("nonce")) {
            return@let it.valueAsStringFromPayload("nonce")
          }
          return@let null
        } ?: parameters.getNonce()
    val requestObject = parameters.getRequestObject()
    val requestUri = parameters.getRequestUri()
    val presentationDefinition = getPresentationDefinition(jwtObject)
    val presentationDefinitionUri =
        jwtObject?.let {
          if (it.containsKey("presentation_definition_uri")) {
            return@let it.valueAsStringFromPayload("presentation_definition_uri")
          }
          return@let null
        } ?: parameters.getPresentationDefinitionUri()
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
        presentationDefinition = presentationDefinition,
        presentationDefinitionUri = presentationDefinitionUri)
  }

  private suspend fun getRequestObject(): JwtObject? {
    if (parameters.hasRequestUri()) {
      val requestUri = parameters.getRequestUri()
      val requestObjectResponse = HttpClient.get(requestUri)
      return JoseHandler.parse(requestObjectResponse.toString())
    }
    if (parameters.hasRequestObject()) {
      return JoseHandler.parse(parameters.getRequestObject())
    }
    return null
  }

  private suspend fun getPresentationDefinition(jwtObject: JwtObject?): PresentationDefinition? {
    if (jwtObject != null) {
      if (jwtObject.containsKey("presentation_definition")) {
        val definition = jwtObject.valueAsObjectFromPayload("presentation_definition")
        val jsonString = JsonUtils.write(definition)
        return JsonUtils.read(jsonString, PresentationDefinition::class.java)
      }
      if (jwtObject.containsKey("presentation_definition_uri")) {
        val definitionUri = jwtObject.valueAsStringFromPayload("presentation_definition_uri")
        val response = HttpClient.get(definitionUri)
        return JsonUtils.read(response.toString(), PresentationDefinition::class.java)
      }
    }
    if (parameters.hasPresentationDefinitionObject()) {
      val definition = parameters.getPresentationDefinitionObject()
      return JsonUtils.read(definition, PresentationDefinition::class.java)
    }
    if (parameters.hasPresentationDefinitionUri()) {
      val definitionUri = parameters.getPresentationDefinitionUri()
      val response = HttpClient.get(definitionUri)
      return JsonUtils.read(response.toString(), PresentationDefinition::class.java)
    }
    return null
  }
}
