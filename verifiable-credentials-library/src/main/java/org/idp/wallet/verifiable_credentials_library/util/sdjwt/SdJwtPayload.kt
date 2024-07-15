package org.idp.wallet.verifiable_credentials_library.util.sdjwt

data class SdJwtPayload(
    val plainPayload: List<SdJwtElement>,
    val structuredPayload: Map<String, List<SdJwtElement>>,
)

data class SdJwtElement(val key: String, val value: Any, val disclosable: Boolean)
