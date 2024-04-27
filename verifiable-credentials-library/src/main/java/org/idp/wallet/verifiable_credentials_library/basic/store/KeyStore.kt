package org.idp.wallet.verifiable_credentials_library.basic.store

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class KeyStore(val context: Context) {

  private val encryptedSharedPreferences =
      EncryptedSharedPreferences.create(
          context,
          "secret_shared_prefs",
          MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

  fun store(keyId: String, jwk: String) {
    encryptedSharedPreferences.edit().putString(keyId, jwk).apply()
  }

  fun find(keyId: String): String? {
    return encryptedSharedPreferences.getString(keyId, null)
  }

  fun contains(keyId: String): Boolean {
    return encryptedSharedPreferences.contains(keyId)
  }
}
