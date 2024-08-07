package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

data class CredentialIssuanceResult(
    val id: String,
    val issuer: String,
    val credentialConfigurationId: String,
    val credential: String?,
    val transactionId: String?,
    val cNonce: String?,
    val cNonceExpiresIn: Int?,
    val notificationId: String?,
    val status: CredentialIssuanceResultStatus
)

enum class CredentialIssuanceResultStatus {
  PENDING,
  SUCCESS,
}
