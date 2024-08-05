package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

interface CredentialIssuanceResultRepository {
  suspend fun register(credentialIssuanceResult: CredentialIssuanceResult)

  suspend fun findAll(): List<CredentialIssuanceResult>

  suspend fun update(credentialIssuanceResult: CredentialIssuanceResult)

  suspend fun delete(id: String)
}
