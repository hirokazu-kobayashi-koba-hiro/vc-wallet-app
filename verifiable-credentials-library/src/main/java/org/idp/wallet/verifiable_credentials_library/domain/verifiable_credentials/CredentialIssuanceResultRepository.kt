package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

interface CredentialIssuanceResultRepository {
  suspend fun register(subject: String, credentialIssuanceResult: CredentialIssuanceResult)

  suspend fun findAll(subject: String): List<CredentialIssuanceResult>

  suspend fun get(subject: String, id: String): CredentialIssuanceResult

  suspend fun update(subject: String, credentialIssuanceResult: CredentialIssuanceResult)

  suspend fun delete(subject: String, id: String)
}
