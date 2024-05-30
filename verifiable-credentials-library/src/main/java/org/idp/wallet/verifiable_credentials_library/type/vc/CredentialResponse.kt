package org.idp.wallet.verifiable_credentials_library.type.vc
data class CredentialResponse(
    val credential: String?,
    val transactionId: String?,
    val cNonce: String?,
    val cNonceExpiresIn: Int?
) {}
