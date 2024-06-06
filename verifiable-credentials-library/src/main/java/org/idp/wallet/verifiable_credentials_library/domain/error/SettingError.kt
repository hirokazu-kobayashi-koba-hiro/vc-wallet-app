package org.idp.wallet.verifiable_credentials_library.domain.error

enum class SettingError(val code: String, val description: String) {
  NOT_FOUND_WALLET_CONFIG(
      "0001",
      "Wallet configuration file is not found in app assets. Configuration file name must be wallet-configuration.json")
}
