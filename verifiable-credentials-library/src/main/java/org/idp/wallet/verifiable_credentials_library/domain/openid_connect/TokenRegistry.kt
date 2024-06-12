package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

class TokenRegistry(private val values: MutableMap<String, TokenRecord> = mutableMapOf()) {

  fun add(scope: String, tokenRecord: TokenRecord) {
    values[scope] = tokenRecord
  }

  fun find(scope: String): TokenRecord? {
    return values[scope]
  }

  fun delete(scope: String) {
    values.remove(scope)
  }

  fun deleteAll() {
    values.keys.forEach { values.remove(it) }
  }
}
