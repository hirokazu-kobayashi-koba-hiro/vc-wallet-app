package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.basic.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.basic.store.KeyStore
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.handler.oauth.OAuthRequestHandler
import org.idp.wallet.verifiable_credentials_library.handler.verifiable_presentation.VerifiablePresentationHandler
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationInteractor
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
            registry, OAuthRequestHandler(walletConfigurationService, mock))
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
  ) {
    verifiablePresentationHandler.handleVpRequest(
        context,
        "https://client.example.org/universal-link?response_type=vp_token&client_id=https%3A%2F%2Fclient.example.org%2Fcallback&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcallback&presentation_definition=%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7B%0A%20%20%20%20%22id%22%3A%20%22example%20with%20selective%20disclosure%22%2C%0A%20%20%20%20%22input_descriptors%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%22id%22%3A%20%22ID%20card%20with%20constraints%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22format%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22ldp_vc%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22proof_type%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22Ed25519Signature2018%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%5D%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22constraints%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22limit_disclosure%22%3A%20%22required%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22fields%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22path%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22%24.vc.type%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%5D%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22filter%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22type%22%3A%20%22string%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22pattern%22%3A%20%22ExampleDegreeCredential%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%5D%0A%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%5D%0A%7D&client_metadata=%7B%0A%20%20%20%20%22vp_formats%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%22jwt_vp%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%22alg%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22EdDSA%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22ES256K%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%5D%0A%20%20%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%20%20%22ldp_vp%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%22proof_type%22%3A%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22Ed25519Signature2018%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%5D%0A%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%7D%0A%7D",
        interactor)
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
