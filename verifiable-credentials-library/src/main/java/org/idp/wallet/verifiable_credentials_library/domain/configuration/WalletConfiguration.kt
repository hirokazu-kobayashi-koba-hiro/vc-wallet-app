package org.idp.wallet.verifiable_credentials_library.domain.configuration

data class WalletConfiguration(val issuer: String, var privateKey: String = "") {}
