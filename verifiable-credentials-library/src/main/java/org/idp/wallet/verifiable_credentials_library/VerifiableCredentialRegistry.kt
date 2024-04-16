package org.idp.wallet.verifiable_credentials_library

import android.content.Context

class VerifiableCredentialRegistry(context: Context) {

    private val values: MutableMap<String, VerifiableCredentialsRecords> = mutableMapOf()

    fun save(credentialIssuer: String, record: VerifiableCredentialsRecord) {
        val optVerifiableCredentialsRecords = values[credentialIssuer]
        val verifiableCredentialsRecords = optVerifiableCredentialsRecords?: VerifiableCredentialsRecords()
        val addedRecords = verifiableCredentialsRecords.add(record)
        values[credentialIssuer] = addedRecords
    }

    fun getAll(): Map<String, VerifiableCredentialsRecords> {
       return values
    }

    fun find(credentialIssuer: String): VerifiableCredentialsRecords? {
        return values[credentialIssuer]
    }
}