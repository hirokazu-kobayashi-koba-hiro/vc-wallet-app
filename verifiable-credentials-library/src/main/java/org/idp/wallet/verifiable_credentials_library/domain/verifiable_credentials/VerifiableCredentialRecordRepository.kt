package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

interface VerifiableCredentialRecordRepository {
  suspend fun save(sub: String, record: VerifiableCredentialsRecord)

  suspend fun find(
      sub: String,
  ): VerifiableCredentialsRecords

  suspend fun getAllAsCollection(
      sub: String,
  ): VerifiableCredentialsRecords

  suspend fun find(sub: String, credentialIssuer: String): VerifiableCredentialsRecords?
}
