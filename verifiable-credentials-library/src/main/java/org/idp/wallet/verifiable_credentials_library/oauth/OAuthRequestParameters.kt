package org.idp.wallet.verifiable_credentials_library.oauth

import java.util.stream.Collectors
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.ResponseType
import org.idp.wallet.verifiable_credentials_library.type.vp.ClientIdScheme

class OAuthRequestParameters(val params: Map<String, List<String>>) {

  fun getScope(): Set<String> {
    val scopesValue = getFirstOrEmptyAsString("scope")
    return scopesValue.split(" ").stream().collect(Collectors.toSet())
  }

  fun hasScope(): Boolean {
    return params.containsKey("scope")
  }

  fun getResponseType(): ResponseType {
    return ResponseType.of(getFirstOrEmptyAsString("response_type"))
  }

  fun hasResponseType(): Boolean {
    return params.containsKey("response_type")
  }

  fun getClientId(): String {
    return getFirstOrEmptyAsString("client_id")
  }

  fun hasClientId(): Boolean {
    return params.containsKey("client_id")
  }

  fun getRedirectUri(): String {
    return getFirstOrEmptyAsString("redirect_uri")
  }

  fun hasRedirectUri(): Boolean {
    return params.containsKey("redirect_uri")
  }

  fun getState(): String {
    return getFirstOrEmptyAsString("state")
  }

  fun hasState(): Boolean {
    return params.containsKey("state")
  }

  fun getResponseMode(): ResponseMode {
    return ResponseMode.of(getFirstOrEmptyAsString("response_mode"))
  }

  fun hasResponseMode(): Boolean {
    return params.containsKey("response_mode")
  }

  fun getNonce(): String {
    return getFirstOrEmptyAsString("nonce")
  }

  fun hasNonce(): Boolean {
    return params.containsKey("nonce")
  }

  fun getRequestObject(): String {
    return getFirstOrEmptyAsString("request")
  }

  fun hasRequestObject(): Boolean {
    return params.containsKey("request")
  }

  fun getRequestUri(): String {
    return getFirstOrEmptyAsString("request_uri")
  }

  fun hasRequestUri(): Boolean {
    return params.containsKey("request_uri")
  }

  fun getPresentationDefinitionObject(): String {
    return getFirstOrEmptyAsString("presentation_definition")
  }

  fun hasPresentationDefinitionObject(): Boolean {
    return params.containsKey("presentation_definition")
  }

  fun getPresentationDefinitionUri(): String {
    return getFirstOrEmptyAsString("presentation_definition_uri")
  }

  fun hasPresentationDefinitionUri(): Boolean {
    return params.containsKey("presentation_definition_uri")
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
    return ClientIdScheme.of("client_id_scheme")
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
    return params.getOrDefault(key, listOf()).first()
  }
}
