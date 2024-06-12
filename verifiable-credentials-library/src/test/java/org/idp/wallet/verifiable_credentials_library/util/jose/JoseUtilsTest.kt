package org.idp.wallet.verifiable_credentials_library.util.jose

import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test

class JoseUtilsTest {

  @Test
  fun to_generate_ec_key() {
    val ecKey = JoseUtils.generateECKey("123")
    println(ecKey)
    Assert.assertTrue(
        ecKey.contains(
            """
            "kid":"123"
        """
                .trimIndent()))
  }

  @Test
  fun sign() {
    val header = mapOf("type" to "JWT")
    val payload = mapOf("iss" to "iss", "client_id" to "client_id")
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
    val signedValue = JoseUtils.sign(header, payload, jwk)
    assertTrue(signedValue.contains("."))
  }

  @Test
  fun sign2() {
    val header = mapOf("type" to "JWT")
    val payload =
        """
            {
                "redirect_uri": "https://client.example.org/callback",
                "response_type": "vp_token",
                "presentation_definition": {
                    "id": "example with selective disclosure",
                    "input_descriptors": [
                        {
                            "id": "ID card with constraints",
                            "format": {
                                "ldp_vc": {
                                    "proof_type": [
                                        "Ed25519Signature2018"
                                    ]
                                }
                            },
                            "constraints": {
                                "limit_disclosure": "required",
                                "fields": [
                                    {
                                        "path": [
                                            "${'$'}.vc.type"
                                        ],
                                        "filter": {
                                            "type": "string",
                                            "pattern": "IDCardCredential"
                                        }
                                    },
                                    {
                                        "path": [
                                            "${'$'}.credentialSubject.given_name"
                                        ]
                                    },
                                    {
                                        "path": [
                                            "${'$'}.credentialSubject.family_name"
                                        ]
                                    },
                                    {
                                        "path": [
                                            "${'$'}.credentialSubject.birthdate"
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                },
                "client_metadata": {
                    "issuer": "http://localhost:8080/123",
                    "client_id": "privateKeyJwt",
                    "client_secret": "privateKeyJwtSecret",
                    "client_id_issued_at": 2893256800,
                    "client_secret_expires_at": 2893276800,
                    "redirect_uris": [
                        "https://client.example.org/callback",
                        "https://client.example.org/callback2",
                        "http://localhost:8081/callback",
                        "https://www.certification.openid.net/test/a/idp_oidc_basic/callback",
                        "https://localhost.emobix.co.uk:8443/test/a/idp_oidc_basic/callback",
                        "https://localhost.emobix.co.uk:8443/test/a/idp_oidc_implicit/callback",
                        "https://localhost.emobix.co.uk:8443/test/a/idp_oidc_hybrid/callback"
                    ],
                    "response_types": [
                        "code",
                        "token",
                        "id_token",
                        "code token",
                        "code token id_token",
                        "token id_token",
                        "code id_token",
                        "none",
                        "vp_token",
                        "vp_token id_token"
                    ],
                    "grant_types": [
                        "authorization_code",
                        "refresh_token",
                        "password",
                        "client_credentials"
                    ],
                    "scope": "openid profile email address phone offline_access account transfers read write",
                    "client_name": "My Example Client",
                    "client_name#ja-Jpan-JP": "クライアント名",
                    "token_endpoint_auth_method": "private_key_jwt",
                    "logo_uri": "https://client.example.org/logo.png",
                    "jwks_uri": "https://client.example.org/my_public_keys.jwks",
                    "application_type": "web",
                    "jwks": "{\n    \"keys\": [\n        {\n            \"kty\": \"RSA\",\n            \"e\": \"AQAB\",\n            \"use\": \"sig\",\n            \"kid\": \"client_secret_key\",\n            \"alg\": \"RS256\",\n            \"n\": \"sFSqsWu2koU69oG67L5wsVGwzkye80Bd9lmOfiSkSTXyc8IKl4gwmj9tjzxxA1pGYi4SKEQaBYNl8JrGhttBcbtraqwaS5Q6jpG24C1z9njUumJWJneA3EJ9Lpun9d3uCA3b_71XnK5Pr-VtwzpU6z8VGNMZhl8rZ5p1L0syMpZ03y5tSWVMntceiqNaFuJCFXGMVSlp6vrVCqpcM4r035tUR-PwjSynpxe7OGlQpHVSvBCbXJJufi0QxIIjdPx2ka586TlvFjVu0QBEcEon_BMrPDWPD1aaAEcSPM9U7fWzlK6btJ8d37TXZ0_rRPQ_tVeZAlDnRclehHkKflkNjw\"\n        },{\n    \"kty\": \"RSA\",\n    \"e\": \"AQAB\",\n    \"use\": \"sig\",\n    \"kid\": \"client_secret_key_2040\",\n    \"alg\": \"RS256\",\n    \"n\": \"tndIPiYnTw8UlbvU4GSD77tPwePNpu1VYmTWmrOA1etvNV7xeU10lHPscmbr702bbo5adEVwWsqwHkS0lvtBBepb3BKhFwl84_Ffqp-P_rqlduQ3Xnri5BfesreOy6nZQcQ95OSR0M4HYgfhrsMXCxQsA1GCDCI7oiKm43icTxaPoH232qfJFG_rInHBEokO-BCK_0Ct-to6dyRLxlDgCoKR4LnWJ_ETfxzb9LSdp6mO3ccD9r9Qit7tgbv-vBzGtMW9Yd_iwbHMo2qRRMnbQMHHK3Vip2xCEIi1v3HC--UUXuecJ_SYG2D69UKC3hvO54ljWKsIyeGAkcTyDZER\"\n}    ]\n}",
                    "authorization_details_types": [
                        "payment_initiation",
                        "account_information",
                        "openid_credential"
                    ]
                }
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
    val signedValue = JoseUtils.sign(header, payload, jwk)
    println(signedValue)
    assertTrue(signedValue.contains("."))
  }

  @Test
  fun sign3() {
    val header = mapOf("type" to "JWT")
    val payload =
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
    val signedValue = JoseUtils.sign(header, payload, jwk)
    println(signedValue)
    assertTrue(signedValue.contains("."))
  }
}
