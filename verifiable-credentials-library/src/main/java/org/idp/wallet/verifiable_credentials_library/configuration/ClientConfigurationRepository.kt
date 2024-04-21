package org.idp.wallet.verifiable_credentials_library.configuration

fun interface ClientConfigurationRepository {
  fun get(clientId: String): ClientConfiguration
}
