package org.idp.wallet.verifiable_credentials_library.domain.configuration

fun interface ClientConfigurationRepository {
  fun get(clientId: String): ClientConfiguration
}
