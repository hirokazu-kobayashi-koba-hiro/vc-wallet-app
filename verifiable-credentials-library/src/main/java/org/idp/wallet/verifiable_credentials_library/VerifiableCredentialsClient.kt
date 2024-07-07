package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.activity.VerifiableCredentialsActivity
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.util.store.KeyStore

object VerifiableCredentialsClient {

  private lateinit var verifiableCredentialsApi: VerifiableCredentialsApi
  private lateinit var verifiablePresentationApi: VerifiablePresentationApi
  private lateinit var walletConfigurationService: WalletConfigurationService

  fun initialize(context: Context, clientId: String) {
    val keyStore = KeyStore(context)
    val assetsReader = AssetsReader(context)
    val registry = VerifiableCredentialRegistry(context)
    registerTestData(registry)
    walletConfigurationService = WalletConfigurationService(keyStore, assetsReader)
    walletConfigurationService.initialize()
    val verifiableCredentialsService = VerifiableCredentialsService(registry, clientId)
    verifiableCredentialsApi = VerifiableCredentialsApi(verifiableCredentialsService)
    val mock = ClientConfigurationRepository {
      return@ClientConfigurationRepository ClientConfiguration()
    }
    verifiablePresentationApi =
        VerifiablePresentationApi(
            registry, VerifiablePresentationRequestContextService(walletConfigurationService, mock))
  }

  fun start(context: Context, request: OpenIdConnectRequest, forceLogin: Boolean = false) {
    VerifiableCredentialsActivity.start(
        context = context, request = request, forceLogin = forceLogin)
  }

  suspend fun handlePreAuthorization(
      context: Context,
      url: String,
      format: String = "vc+sd-jwt",
      interactor: VerifiableCredentialInteractor
  ) {
    verifiableCredentialsApi.handlePreAuthorization(context, url, format, interactor)
  }

  fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return verifiableCredentialsApi.getAllCredentials()
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Any> {
    return verifiablePresentationApi.handleRequest(context, url, interactor)
  }

  private fun registerTestData(registry: VerifiableCredentialRegistry) {
    val header = mapOf("type" to "JWT")
    val payloadValue =
        """
            {
              "vc": {
                "@context": [
                  "https://www.w3.org/ns/credentials/v2",
                  "https://www.w3.org/ns/credentials/examples/v2"
                ],
                "id": "http://university.example/credentials/3732",
                "type": [
                  "VerifiableCredential",
                  "ExampleDegreeCredential"
                ],
                "issuer": "https://university.example/issuers/565049",
                "validFrom": "2010-01-01T00:00:00Z",
                "credentialSubject": {
                  "id": "did:example:ebfeb1f712ebc6f1c276e12ec21",
                  "given_name": "john",
                  "family_name": "alex",
                  "birthdate": "2001-02-03",
                  "degree": {
                    "type": "ExampleBachelorDegree",
                    "name": "Bachelor of Science and Arts"
                  }
                }
              },
              "iss": "https://university.example/issuers/565049",
              "jti": "http://university.example/credentials/3732",
              "sub": "did:example:ebfeb1f712ebc6f1c276e12ec21"
            }
        """
            .trimIndent()
    val jwk =
        """
            {
                "kty": "EC",
                "d": "yIWDrlhnCy3yL9xLuqZGOBFFq4PWGsCeM7Sc_lfeaQQ",
                "use": "sig",
                "crv": "P-256",
                "kid": "access_token",
                "x": "iWJINqt0ySv3kVEvlHbvNkPKY2pPSf1cG1PSx3tRfw0",
                "y": "rW1FdfXK5AQcv-Go6Xho0CR5AbLai7Gp9IdLTIXTSIQ",
                "alg": "ES256"
            }
        """
            .trimIndent()
    val signedValue = JoseUtils.sign(header, payloadValue, jwk)
    val payload = JoseUtils.parse(signedValue).payload()
    val record =
        VerifiableCredentialsRecord("1", "DegreeCredential", "jwt_vc_json", signedValue, payload)
    registry.save("test", record)
  }
}
