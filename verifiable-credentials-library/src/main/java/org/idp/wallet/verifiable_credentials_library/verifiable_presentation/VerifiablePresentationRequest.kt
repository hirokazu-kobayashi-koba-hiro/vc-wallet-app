package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.basic.jose.JwtObject
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

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

  fun filterVerifiableCredential(
      records: VerifiableCredentialsRecords
  ): VerifiableCredentialsRecords {
    return presentationDefinition?.filterVerifiableCredential(records)
        ?: VerifiableCredentialsRecords()
  }
}
