package org.idp.wallet.verifiable_credentials_library.basic.json

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import org.junit.Assert
import org.junit.Test

class JsonPathTest {

  @Test
  fun success_extract_value() {
    val json = """{"hello": "world"}"""
    val read = JsonPath.parse(json)?.read<String>("$.hello") // returns "world"
    val somethingelse =
        JsonPath.parse(json)
            ?.read<String>("$.somethingelse") // returns null since "somethingelse" key not found
    Assert.assertEquals("world", read)
    Assert.assertNull(somethingelse)
  }

  @Test
  fun success_extract_value_from_vc() {
    val vcJsonString =
        """
            {
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://www.w3.org/2018/credentials/examples/v1"
                ],
                "id": "https://example.com/credentials/1872",
                "type": [
                    "VerifiableCredential",
                    "IDCardCredential"
                ],
                "issuer": {
                    "id": "did:example:issuer"
                },
                "issuanceDate": "2010-01-01T19:23:24Z",
                "credentialSubject": {
                    "given_name": "Fredrik",
                    "family_name": "Strömberg",
                    "birthdate": "1949-01-22"
                },
                "proof": {
                    "type": "Ed25519Signature2018",
                    "created": "2021-03-19T15:30:15Z",
                    "jws": "eyJhb...IAoDA",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "did:example:issuer#keys-1"
                }
            }
        """
            .trimIndent()
    val type = JsonPath.parse(vcJsonString)?.read<List<String>>("$.type")
    println(type)
    Assert.assertEquals(listOf("VerifiableCredential", "IDCardCredential"), type)
  }
}
