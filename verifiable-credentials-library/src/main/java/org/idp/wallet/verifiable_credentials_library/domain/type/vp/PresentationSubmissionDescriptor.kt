package org.idp.wallet.verifiable_credentials_library.domain.type.vp

data class PresentationSubmissionDescriptor(
    val id: String = "",
    val format: String = "",
    val path: String = "",
    val pathNested: PresentationSubmissionDescriptor? = null
)
