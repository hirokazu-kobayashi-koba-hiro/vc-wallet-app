package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.ClientIdScheme
import org.idp.wallet.verifiable_credentials_library.util.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.util.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class VerifiablePresentationService(
    private val walletConfiguration: WalletConfiguration,
    private val verifierConfigurationRepository: VerifierConfigurationRepository
) {

  suspend fun create(url: String): VerifiablePresentationRequestContext {
    val parameters = VerifiablePresentationRequestParameters(extractQueries(url))
    val validator = VerifiablePresentationRequestValidator(parameters)
    validator.validate()
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
        verifierConfigurationRepository.get(parameters.getClientId())
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
