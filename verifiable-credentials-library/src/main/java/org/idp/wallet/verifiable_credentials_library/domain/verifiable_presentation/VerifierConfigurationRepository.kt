package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration

fun interface VerifierConfigurationRepository {
  fun get(clientId: String): ClientConfiguration
}
