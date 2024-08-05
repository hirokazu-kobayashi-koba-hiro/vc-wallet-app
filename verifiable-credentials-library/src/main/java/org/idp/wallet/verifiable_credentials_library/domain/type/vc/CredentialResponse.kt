package org.idp.wallet.verifiable_credentials_library.domain.type.vc

data class CredentialResponse(
    val credential: String?,
    val transactionId: String?,
    val cNonce: String?,
    val cNonceExpiresIn: Int?,
    val notificationId: String?
) {}
