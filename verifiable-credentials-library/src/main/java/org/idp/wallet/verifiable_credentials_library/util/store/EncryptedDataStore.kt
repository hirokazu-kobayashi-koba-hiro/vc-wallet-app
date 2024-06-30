package org.idp.wallet.verifiable_credentials_library.util.store

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedDataStore(val context: Context) {

  private val encryptedSharedPreferences =
      EncryptedSharedPreferences.create(
          context,
          "secret_shared_prefs",
          MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

  fun store(key: String, value: String) {
    encryptedSharedPreferences.edit().putString(key, value).apply()
  }

  fun find(key: String): String? {
    return encryptedSharedPreferences.getString(key, null)
  }

  fun contains(keyId: String): Boolean {
    return encryptedSharedPreferences.contains(keyId)
  }
}
