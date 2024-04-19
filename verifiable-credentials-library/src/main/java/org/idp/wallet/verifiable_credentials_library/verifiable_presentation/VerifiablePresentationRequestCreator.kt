package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.jose.JwtObject
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils

class VerifiablePresentationRequestCreator(private val jwtObject: JwtObject) {

  private val payload = jwtObject.payload()

  suspend fun create(): VerifiablePresentationRequest {
    val presentationDefinition = getPresentationDefinition()
    val clientMeta = getClientMeta()
    return VerifiablePresentationRequest(jwtObject, presentationDefinition, clientMeta)
  }

  private suspend fun getClientMeta(): ClientMetadata? {
    val clientMeta = payload.get("client_metadata")
    if (clientMeta != null) {
      val clientMetaValue = JsonUtils.write(clientMeta)
      return JsonUtils.read(clientMetaValue, ClientMetadata::class.java)
    }
    val clientMetadataUri = payload.get("client_metadata_uri")
    if (clientMetadataUri != null) {
      return fetchClientMetadata(clientMetadataUri as String)
    }
    return null
  }

  private suspend fun fetchClientMetadata(uri: String): ClientMetadata {
    val jsonObject = HttpClient.get(uri)
    return JsonUtils.read(jsonObject.toString(), ClientMetadata::class.java)
  }

  private suspend fun getPresentationDefinition(): PresentationDefinition? {
    val definition = payload.get("presentation_definition")
    if (definition != null) {
      val definitionValue = JsonUtils.write(definition)
      return JsonUtils.read(definitionValue, PresentationDefinition::class.java)
    }
    val definitionUri = payload.get("presentation_definition_uri")
    if (definitionUri != null) {
      return fetchPresentationDefinition(definitionUri as String)
    }
    return null
  }

  private suspend fun fetchPresentationDefinition(definitionUri: String): PresentationDefinition {
    val jsonObject = HttpClient.get(definitionUri)
    return JsonUtils.read(jsonObject.toString(), PresentationDefinition::class.java)
  }
}
