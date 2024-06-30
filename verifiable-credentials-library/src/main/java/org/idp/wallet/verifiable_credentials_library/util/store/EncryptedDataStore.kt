package org.idp.wallet.verifiable_credentials_library.util.store

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedDataStore(val context: Context) : EncryptedDataStoreInterface {

  private val encryptedSharedPreferences =
      EncryptedSharedPreferences.create(
          context,
          "vc_encrypted_shared_prefs",
          MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

  override fun store(key: String, value: String) {
    encryptedSharedPreferences.edit().putString(key, value).apply()
  }

  override fun find(key: String): String? {
    return encryptedSharedPreferences.getString(key, null)
  }

  override fun contains(key: String): Boolean {
    return encryptedSharedPreferences.contains(key)
  }

  override fun delete(key: String) {
    encryptedSharedPreferences.edit().remove(key).apply()
  }
}
