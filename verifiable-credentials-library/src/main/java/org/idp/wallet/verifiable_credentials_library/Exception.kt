package org.idp.wallet.verifiable_credentials_library

import java.lang.RuntimeException

interface VerifiableCredentialsError {
    fun code(): String
    fun description(): String
    fun cause(): Throwable?
}

class NetworkException(
    val code: String, val description: String, cause: Throwable? = null): VerifiableCredentialsError,
    RuntimeException(description, cause) {
    override fun code(): String {
        return code;
    }

    override fun description(): String {
        return description
    }

    override fun cause(): Throwable? {
        return cause
    }

}