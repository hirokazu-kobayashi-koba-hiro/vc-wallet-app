package org.idp.wallet.verifiable_credentials_library.type.vp

import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils

class PresentationSubmission(
    val id: String? = null,
    val definitionId: String? = null,
    val descriptorMap: List<PresentationSubmissionDescriptor>? = null
) {

  fun toJsonString(): String {
    return JsonUtils.write(this)
  }
}
