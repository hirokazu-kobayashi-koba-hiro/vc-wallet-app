package org.idp.wallet.verifiable_credentials_library.verifiable_presentation.vp

import org.idp.wallet.verifiable_credentials_library.type.vp.InputDescriptorDetail
import org.idp.wallet.verifiable_credentials_library.type.vp.PresentationSubmission
import org.idp.wallet.verifiable_credentials_library.type.vp.PresentationSubmissionDescriptor
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class PresentationDefinitionEvaluation(
    val definitionId: String = "",
    val results: Map<InputDescriptorDetail, VerifiableCredentialsRecords> = mapOf()
) {

  fun toSubmission(): PresentationSubmission {
    val id: String = definitionId
    val format: String = ""
    val path: String = ""
    val pathNested: PresentationSubmissionDescriptor? = null
    return PresentationSubmission()
  }

  fun verifiableCredentialRecords(): VerifiableCredentialsRecords {
    val list =
        results
            .map {
              return it.value
            }
            .toList()
    return VerifiableCredentialsRecords(list)
  }
}
