package org.idp.wallet.verifiable_credentials_library.handler.oauth

import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationReader
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestContext
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestParameters
import org.idp.wallet.verifiable_credentials_library.oauth.OauthRequestValidator
import org.idp.wallet.verifiable_credentials_library.type.vp.ClientIdScheme
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.PresentationDefinition

class OAuthRequestHandler(
    private val walletConfigurationReader: WalletConfigurationReader,
    private val clientConfigurationRepository: ClientConfigurationRepository
) {

  suspend fun handleRequest(url: String): OAuthRequestContext {
    val parameters = OAuthRequestParameters(extractQueries(url))
    val validator = OauthRequestValidator(parameters)
    validator.validate()
    val walletConfiguration = walletConfigurationReader.get()
    val clientConfiguration = getClientConfiguration(parameters)
    return OAuthRequestContext(parameters, walletConfiguration, clientConfiguration)
  }

  private suspend fun getPresentationDefinition(
      parameters: OAuthRequestParameters
  ): PresentationDefinition? {
    if (parameters.hasPresentationDefinitionObject()) {
      val definition = parameters.getPresentationDefinitionObject()
      return JsonUtils.read(definition, PresentationDefinition::class.java)
    }
    if (parameters.hasPresentationDefinitionUri()) {
      val definitionUri = parameters.getPresentationDefinitionUri()
      return fetchPresentationDefinition(definitionUri)
    }
    return null
  }

  private suspend fun fetchPresentationDefinition(definitionUri: String): PresentationDefinition {
    val jsonObject = HttpClient.get(definitionUri)
    return JsonUtils.read(jsonObject.toString(), PresentationDefinition::class.java)
  }

  private suspend fun getClientConfiguration(
      parameters: OAuthRequestParameters
  ): ClientConfiguration {
    return when (val scheme = parameters.getClientIdScheme()) {
      ClientIdScheme.redirect_uri -> {
        val clientMetadata = getClientMetadata(parameters)
        JsonUtils.read(clientMetadata, ClientConfiguration::class.java)
      }
      ClientIdScheme.pre_registered -> {
        clientConfigurationRepository.get(parameters.getClientId())
      }
      else -> {
        throw RuntimeException()
      }
    }
  }

  private suspend fun getClientMetadata(parameters: OAuthRequestParameters): String {
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
