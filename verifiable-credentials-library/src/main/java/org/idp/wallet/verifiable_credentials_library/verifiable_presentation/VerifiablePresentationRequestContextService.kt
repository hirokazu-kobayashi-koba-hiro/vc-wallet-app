package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.type.vp.ClientIdScheme

class VerifiablePresentationRequestContextService(
    private val walletConfigurationService: WalletConfigurationService,
    private val clientConfigurationRepository: ClientConfigurationRepository
) {

  suspend fun create(url: String): VerifiablePresentationRequestContext {
    val parameters = VerifiablePresentationRequestParameters(extractQueries(url))
    val validator = VerifiablePresentationRequestValidator(parameters)
    validator.validate()
    val walletConfiguration = walletConfigurationService.getConfiguration()
    val clientConfiguration = getClientConfiguration(parameters)
    val authorizationRequestCreationService = AuthorizationRequestCreationService(parameters)
    val oauthRequest = authorizationRequestCreationService.create()
    val verifiablePresentationRequestContext =
        VerifiablePresentationRequestContext(
            parameters, oauthRequest, walletConfiguration, clientConfiguration)
    val verifier = VerifiablePresentationRequestVerifier(verifiablePresentationRequestContext)
    verifier.verify()
    return verifiablePresentationRequestContext
  }

  private suspend fun getClientConfiguration(
      parameters: VerifiablePresentationRequestParameters
  ): ClientConfiguration {
    return when (val scheme = parameters.getClientIdScheme()) {
      ClientIdScheme.redirect_uri -> {
        val clientMetadata = getClientMetadata(parameters)
        JsonUtils.read(clientMetadata, ClientConfiguration::class.java)
      }
      // pre-registered
      else -> {
        clientConfigurationRepository.get(parameters.getClientId())
      }
    }
  }

  private suspend fun getClientMetadata(
      parameters: VerifiablePresentationRequestParameters
  ): String {
    if (parameters.hasClientMetadata()) {
      return parameters.clientMetadata()
    }
    if (parameters.hasClientMetadataUri()) {
      val url = parameters.clientMetadataUri()
      val response = HttpClient.get(url)
      return response.toString()
    }
    throw RuntimeException()
  }
}
