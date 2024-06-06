package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import java.util.stream.Collectors
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.ResponseMode
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.ResponseType
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.ClientIdScheme

class VerifiablePresentationRequestParameters(val params: Map<String, List<String>>) {

  fun getScope(): Set<String>? {
    if (params.containsKey("scope")) {
      val scopesValue = getFirstOrEmptyAsString("scope")
      return scopesValue.split(" ").stream().collect(Collectors.toSet())
    }
    return null
  }

  fun getResponseType(): ResponseType? {
    if (params.containsKey("response_type")) {
      return ResponseType.of(getFirstOrEmptyAsString("response_type"))
    }
    return null
  }

  fun getClientId(): String {
    return getFirstOrEmptyAsString("client_id")
  }

  fun hasClientId(): Boolean {
    return params.containsKey("client_id")
  }

  fun getRedirectUri(): String? {
    if (params.containsKey("redirect_uri")) {
      return getFirstOrEmptyAsString("redirect_uri")
    }
    return null
  }

  fun getState(): String? {
    if (params.containsKey("state")) {
      return getFirstOrEmptyAsString("state")
    }
    return null
  }

  fun getResponseMode(): ResponseMode? {
    if (params.containsKey("response_mode")) {
      return ResponseMode.of(getFirstOrEmptyAsString("response_mode"))
    }
    return null
  }

  fun getNonce(): String? {
    if (params.containsKey("nonce")) {
      return getFirstOrEmptyAsString("nonce")
    }
    return null
  }

  fun getRequestObject(): String? {
    if (params.containsKey("request")) {
      return getFirstOrEmptyAsString("request")
    }
    return null
  }

  fun getRequestUri(): String? {
    if (params.containsKey("request_uri")) {
      return getFirstOrEmptyAsString("request_uri")
    }
    return null
  }

  fun getPresentationDefinitionObject(): String? {
    if (params.containsKey("presentation_definition")) {
      return getFirstOrEmptyAsString("presentation_definition")
    }
    return null
  }

  fun getPresentationDefinitionUri(): String? {
    if (params.containsKey("presentation_definition_uri")) {
      return getFirstOrEmptyAsString("presentation_definition_uri")
    }
    return null
  }

  fun clientMetadata(): String {
    return getFirstOrEmptyAsString("client_metadata")
  }

  fun hasClientMetadata(): Boolean {
    return params.containsKey("client_metadata")
  }

  fun clientMetadataUri(): String {
    return getFirstOrEmptyAsString("client_metadata_uri")
  }

  fun hasClientMetadataUri(): Boolean {
    return params.containsKey("client_metadata_uri")
  }

  fun getClientIdScheme(): ClientIdScheme {
    return ClientIdScheme.of(getFirstOrEmptyAsString("client_id_scheme"))
  }

  fun getMultiValuedKeys(): List<String> {
    val mutableList = mutableListOf<String>()
    params.forEach { (key, value) ->
      if (value.size > 1) {
        mutableList.add(key)
      }
    }
    return mutableList
  }

  private fun getFirstOrEmptyAsString(key: String): String {
    if (!params.containsKey(key)) {
      return ""
    }
    return params.getOrDefault(key, listOf("")).first()
  }
}
