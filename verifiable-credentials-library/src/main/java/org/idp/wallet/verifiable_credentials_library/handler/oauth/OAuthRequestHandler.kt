package org.idp.wallet.verifiable_credentials_library.handler.oauth

import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.basic.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationReader
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestContext
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestCreationService
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestParameters
import org.idp.wallet.verifiable_credentials_library.oauth.OAuthRequestVerifier
import org.idp.wallet.verifiable_credentials_library.oauth.OauthRequestValidator
import org.idp.wallet.verifiable_credentials_library.type.vp.ClientIdScheme

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
    val oauthRequestCreationService = OAuthRequestCreationService(parameters)
    val oauthRequest = oauthRequestCreationService.create()
    val oAuthRequestContext =
        OAuthRequestContext(parameters, oauthRequest, walletConfiguration, clientConfiguration)
    val verifier = OAuthRequestVerifier(oAuthRequestContext)
    verifier.verify()
    return oAuthRequestContext
  }

  private suspend fun getClientConfiguration(
      parameters: OAuthRequestParameters
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
