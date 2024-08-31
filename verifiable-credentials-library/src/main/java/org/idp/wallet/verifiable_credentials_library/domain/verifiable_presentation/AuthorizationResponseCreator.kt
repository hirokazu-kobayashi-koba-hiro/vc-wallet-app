package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.PresentationSubmission
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.PresentationSubmissionDescriptor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils

class AuthorizationResponseCreator(
    private val verifiablePresentationRequestContext: VerifiablePresentationRequestContext,
    private val evaluation: PresentationDefinitionEvaluation
) {

  fun create(): AuthorizationResponse {
    val issuer = verifiablePresentationRequestContext.getIssuer()
    val redirectUri = verifiablePresentationRequestContext.getRedirectUri()
    val vpToken = createVpToken()
    val presentationSubmission = createPresentationSubmission()
    return AuthorizationResponse(
        issuer = issuer,
        redirectUri = redirectUri,
        vpToken = vpToken,
        presentationSubmission = presentationSubmission)
  }

  private fun createPresentationSubmission(): PresentationSubmission {
    val id = UUID.randomUUID().toString()
    val definitionId = evaluation.definitionId
    val descriptorMap = createPresentationSubmissionDescriptorMap()
    return PresentationSubmission(
        id = id, definitionId = definitionId, descriptorMap = descriptorMap)
  }

  private fun createPresentationSubmissionDescriptorMap(): List<PresentationSubmissionDescriptor> {
    val list = mutableListOf<PresentationSubmissionDescriptor>()
    var index = 0
    evaluation.results.forEach { (descriptor, records) ->
      records.forEach {
        val id = descriptor.id
        val format = it.format
        val path = "$.verifiableCredential[$index]"
        list.add(PresentationSubmissionDescriptor(id, format, path))
        index += 1
      }
    }
    return list
  }

  private fun createVpToken(): String {
    val records = evaluation.verifiableCredentialRecords()
    val privateKey = verifiablePresentationRequestContext.walletConfiguration.privateKey
    val header = mapOf<String, Any>("type" to "JWT")
    val payload = mutableMapOf<String, Any>()
    payload.put("id", UUID.randomUUID().toString())
    payload.put("type", listOf("VerifiablePresentation"))
    payload.put("verifiableCredential", records.rawVcList())
    payload.put(
        "@context",
        listOf(
            "https://www.w3.org/ns/credentials/v2",
            "https://www.w3.org/ns/credentials/examples/v2"))
    return JoseUtils.sign(additionalHeaders = header, payload = payload, privateKey = privateKey)
  }
}
