package org.idp.wallet.verifiable_credentials_library.util.json

import org.junit.Test

class JsonLdUtilsTest {

  @Test
  fun normalize() {
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
  }
}
