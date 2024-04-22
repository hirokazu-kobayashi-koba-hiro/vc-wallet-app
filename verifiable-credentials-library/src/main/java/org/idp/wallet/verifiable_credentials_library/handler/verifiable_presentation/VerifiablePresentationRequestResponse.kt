package org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecord

class VerifiablePresentationRequestResponse(
    val verifiableCredentialsRecords: List<VerifiableCredentialsRecord>?
) {}
