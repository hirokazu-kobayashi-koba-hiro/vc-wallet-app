package org.idp.wallet.verifiable_credentials_library.domain.configuration

import org.idp.wallet.verifiable_credentials_library.domain.error.SettingError
import org.idp.wallet.verifiable_credentials_library.domain.error.SettingInvalidException
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.util.resource.ResourceReader
import org.idp.wallet.verifiable_credentials_library.util.store.KeyStore

class WalletConfigurationService(
    private val keyStore: KeyStore,
    private val resourceReader: ResourceReader
) {
  private val keyId = "vc_wallet_jwt_key"

  fun initialize() {
    if (!keyStore.contains(keyId)) {
      val ecKey = JoseUtils.generateECKey(keyId)
      keyStore.store(keyId, ecKey)
    }
  }

  fun getConfiguration(): WalletConfiguration {
    try {
      val configurationValue = resourceReader.read("wallet-configuration.json")
      val walletConfiguration = JsonUtils.read(configurationValue, WalletConfiguration::class.java)
      val key = keyStore.find(keyId)
      key?.let {
        val jwks = JoseUtils.transformJwksAsString(it)
        walletConfiguration.jwks = jwks
      }
      return walletConfiguration
    } catch (e: Exception) {
      throw SettingInvalidException(SettingError.NOT_FOUND_WALLET_CONFIG, e)
    }
  }
}
