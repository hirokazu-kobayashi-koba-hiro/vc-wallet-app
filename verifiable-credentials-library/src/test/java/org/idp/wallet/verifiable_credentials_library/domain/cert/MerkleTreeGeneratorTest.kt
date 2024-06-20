package org.idp.wallet.verifiable_credentials_library.domain.cert

import org.idp.wallet.verifiable_credentials_library.util.json.JsonLdUtils
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MerkleTreeGeneratorTest {

  @Test
  fun generate() {
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
  }
}
