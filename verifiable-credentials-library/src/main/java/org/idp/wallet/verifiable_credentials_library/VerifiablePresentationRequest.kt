package org.idp.wallet.verifiable_credentials_library

import org.idp.wallet.verifiable_credentials_library.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.jose.JwtObject
import org.json.JSONObject
import java.util.Objects

class VerifiablePresentationRequest(
    val jwtObject: JwtObject,
    val presentationDefinition: PresentationDefinition?,
    val clientMeta: ClientMetadata?
) {

    fun responseType(): String {
        return jwtObject.valueAsStringFromPayload("response_type")
    }

    fun responseMode(): String {
        return jwtObject.valueAsStringFromPayload("response_mode")
    }

    fun scope(): String {
        return jwtObject.valueAsStringFromPayload("scope")
    }

    fun nonce(): String {
        return jwtObject.valueAsStringFromPayload("nonce")
    }

    fun redirectUri(): String {
        return jwtObject.valueAsStringFromPayload("redirect_uri")
    }

    fun state(): String {
        return jwtObject.valueAsStringFromPayload("state")
    }
}

class PresentationDefinition(val values: JSONObject) {


    fun valueAsObject(key: String): JSONObject? {
        return values.optJSONObject(key)
    }
    fun valueAsStringOrEmpty(key: String): String {
        return values.optString(key, "")
    }
}

class ClientMetadata(val values: JSONObject) {

    fun valueAsObject(key: String): JSONObject? {
        return values.optJSONObject(key)
    }
    fun valueAsStringOrEmpty(key: String): String {
        return values.optString(key, "")
    }
}
class VerifiablePresentationRequestCreator(private val jwtObject: JwtObject) {

    val payload = jwtObject.payload()
    suspend fun create():VerifiablePresentationRequest {
        val presentationDefinition = getPresentationDefinition()
        val clientMeta = getClientMeta()
        return VerifiablePresentationRequest(jwtObject, presentationDefinition, clientMeta)
    }

    private suspend fun getClientMeta(): ClientMetadata? {
        val definition = payload.get("client_metadata")
        if (Objects.nonNull(definition)) {
            return ClientMetadata(definition as JSONObject)
        }
        val definitionUri = payload.get("client_metadata_uri")
        if (Objects.nonNull(definitionUri)) {
            return fetchClientMetadata()
        }
        return null
    }

    private suspend fun fetchClientMetadata(): ClientMetadata {
        val definitionUri = payload.get("client_metadata_uri") as String
        val jsonObject = HttpClient.get(definitionUri)
        return ClientMetadata(jsonObject)
    }

    private suspend fun getPresentationDefinition(): PresentationDefinition? {
        val definition = payload.get("presentation_definition")
        if (Objects.nonNull(definition)) {
            return PresentationDefinition(definition as JSONObject)
        }
        val definitionUri = payload.get("presentation_definition_uri")
        if (Objects.nonNull(definitionUri)) {
            return fetchPresentationDefinition()
        }
        return null
    }

    private suspend fun fetchPresentationDefinition(): PresentationDefinition {
        val definitionUri = payload.get("presentation_definition_uri") as String
        val jsonObject = HttpClient.get(definitionUri)
        return PresentationDefinition(jsonObject)
    }
}