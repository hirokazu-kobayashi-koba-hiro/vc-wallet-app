package org.idp.wallet.verifiable_credentials_library.configuration

import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.basic.resource.ResourceReader
import org.idp.wallet.verifiable_credentials_library.error.SettingError
import org.idp.wallet.verifiable_credentials_library.error.SettingInvalidException

class WalletConfigurationReader(private val resourceReader: ResourceReader) {

    fun get(): WalletConfiguration {
        try {
            val configurationValue = resourceReader.read("wallet-configuration.json")
            return JsonUtils.read(configurationValue, WalletConfiguration::class.java)
        } catch (e: Exception) {
            throw SettingInvalidException(SettingError.NOT_FOUND_WALLET_CONFIG, e)
        }
    }
}