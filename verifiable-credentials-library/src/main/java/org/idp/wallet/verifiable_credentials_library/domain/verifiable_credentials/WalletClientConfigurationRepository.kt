package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration

interface WalletClientConfigurationRepository {
  suspend fun register(issuer: String, configuration: ClientConfiguration)

  suspend fun find(issuer: String): ClientConfiguration?
}
