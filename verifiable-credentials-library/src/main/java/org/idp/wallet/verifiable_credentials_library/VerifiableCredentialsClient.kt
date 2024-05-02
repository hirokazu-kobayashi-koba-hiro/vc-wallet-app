package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.basic.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.basic.store.KeyStore
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation.VerifiablePresentationHandler
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationRequestContextService
import org.json.JSONObject

object VerifiableCredentialsClient {

  private lateinit var verifiableCredentialsService: VerifiableCredentialsService
  private lateinit var verifiablePresentationHandler: VerifiablePresentationHandler
  private lateinit var walletConfigurationService: WalletConfigurationService

  fun initialize(context: Context, clientId: String) {
    val keyStore = KeyStore(context)
    val assetsReader = AssetsReader(context)
    val registry = VerifiableCredentialRegistry(context)
    registerTestData(registry)
    walletConfigurationService = WalletConfigurationService(keyStore, assetsReader)
    walletConfigurationService.initialize()
    verifiableCredentialsService = VerifiableCredentialsService(registry, clientId)
    val mock = ClientConfigurationRepository {
      return@ClientConfigurationRepository ClientConfiguration()
    }
    verifiablePresentationHandler =
        VerifiablePresentationHandler(
            registry, VerifiablePresentationRequestContextService(walletConfigurationService, mock))
  }

  suspend fun requestVCI(url: String, format: String = "vc+sd-jwt"): JSONObject {
    return verifiableCredentialsService.requestVCI(url, format)
  }

  fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return verifiableCredentialsService.getAllCredentials()
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Any> {
    return verifiablePresentationHandler.handleRequest(context, url, interactor)
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
    val signedValue = JoseHandler.sign(header, payloadValue, jwk)
    val payload = JoseHandler.parse(signedValue).payload()
    val record = VerifiableCredentialsRecord("1", "jwt_vc_json", signedValue, payload)
    registry.save("test", record)
  }
}
