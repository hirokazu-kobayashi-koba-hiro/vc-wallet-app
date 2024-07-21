package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import android.content.Context

class VerifiableCredentialRegistry(context: Context) {

  private val values: MutableMap<String, VerifiableCredentialsRecords> = mutableMapOf()

  fun save(record: VerifiableCredentialsRecord) {
    val optVerifiableCredentialsRecords = values[record.issuer]
    val verifiableCredentialsRecords =
        optVerifiableCredentialsRecords ?: VerifiableCredentialsRecords()
    val addedRecords = verifiableCredentialsRecords.add(record)
    values[record.issuer] = addedRecords
  }

  fun getAll(): Map<String, VerifiableCredentialsRecords> {
    return values
  }

  fun getAllAsCollection(): VerifiableCredentialsRecords {
    val list = mutableListOf<VerifiableCredentialsRecord>()
    values.forEach { it.value.forEach { record -> list.add(record) } }
    return VerifiableCredentialsRecords(list)
  }

  fun find(credentialIssuer: String): VerifiableCredentialsRecords? {
    return values[credentialIssuer]
  }
}
