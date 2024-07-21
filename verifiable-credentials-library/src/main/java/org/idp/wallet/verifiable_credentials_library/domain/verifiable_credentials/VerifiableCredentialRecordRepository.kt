package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

interface VerifiableCredentialRecordRepository {
  suspend fun save(record: VerifiableCredentialsRecord)

  suspend fun getAll(): Map<String, VerifiableCredentialsRecords>

  suspend fun getAllAsCollection(): VerifiableCredentialsRecords

  suspend fun find(credentialIssuer: String): VerifiableCredentialsRecords?
}
