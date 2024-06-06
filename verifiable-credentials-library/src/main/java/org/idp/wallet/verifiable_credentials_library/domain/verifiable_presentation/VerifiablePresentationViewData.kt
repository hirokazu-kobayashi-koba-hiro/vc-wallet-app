package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

class VerifiablePresentationViewData(
    val verifierName: String = "",
    val verifierLogoUri: String = "",
    val credentialType: String = "",
    val purpose: String = ""
) {}
