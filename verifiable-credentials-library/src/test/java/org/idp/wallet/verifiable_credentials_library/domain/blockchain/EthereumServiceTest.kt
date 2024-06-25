package org.idp.wallet.verifiable_credentials_library.domain.blockchain

import org.idp.wallet.verifiable_credentials_library.domain.cert.MerkleTreeGenerator
import org.idp.wallet.verifiable_credentials_library.util.json.JsonLdUtils
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EthereumServiceTest {
  val url = System.getenv("WEB3_URL") ?: ""
  val address = System.getenv("ADDRESS") ?: ""
  val privateKey = System.getenv("PRIVATE_KEY") ?: ""
  val chain = "ethereum_sepolia"
  val verificationMethod = System.getenv("VERIFICATION_METHOD") ?: ""

  @Before
  fun init() {
    EthereumService.init(url)
  }

  @Test
  fun issueTransaction() {
    val credential =
        """
            {
              "issuer": "did:web:assets.dev.trustid.sbi-fc.com",
              "issuanceDate": "2024-01-03T21:57:00Z",
              "type": [
                "VerifiableCredential"
              ],
              "credentialSubject": {
                "id": "did:example:test"
              }
            }
        """
            .trimIndent()
    val map = JsonUtils.read(credential, Map::class.java)
    val normalize = JsonLdUtils.normalize(map)
    println(normalize)
    val merkleTreeGenerator = MerkleTreeGenerator(normalize)
    val blockchainData = merkleTreeGenerator.getBlockchainData()
    println(blockchainData)
    val transactionId =
        EthereumService.issueTransaction(address, privateKey, chain, blockchainData.toString())
    val proof = merkleTreeGenerator.generateProof(transactionId, verificationMethod, chain)
    //     println(Decoder(proof["proofValue"].toString()).decode())
    println(JsonUtils.write(proof))
  }
}
