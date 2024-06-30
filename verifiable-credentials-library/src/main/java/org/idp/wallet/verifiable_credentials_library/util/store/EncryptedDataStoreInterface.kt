package org.idp.wallet.verifiable_credentials_library.util.store

interface EncryptedDataStoreInterface {
  fun store(key: String, value: String)

  fun find(key: String): String?

  fun contains(key: String): Boolean

  fun delete(key: String)
}
