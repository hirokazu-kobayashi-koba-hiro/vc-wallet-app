package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractorCallback
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationViewData
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifierConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp.PresentationDefinitionEvaluation
import org.idp.wallet.verifiable_credentials_library.repository.AppDatabase
import org.idp.wallet.verifiable_credentials_library.repository.VerifiableCredentialRecordDataSource
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifiablePresentationApiTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().getContext()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val repository = VerifiableCredentialRecordDataSource(database)
    val configuration =
        WalletConfiguration(
            issuer = "http://localhost:8080/123",
            privateKey =
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
                    .trimIndent())
    val oauthRequestHandler =
        VerifiablePresentationRequestContextService(
            configuration,
            VerifierConfigurationRepository { it ->
              return@VerifierConfigurationRepository ClientConfiguration()
            })
    VerifiablePresentationApi.initialize(repository, oauthRequestHandler)
  }

  @Test
  fun to_handle_vp_request() {
    runBlocking {
      val presentationDefinition =
          """
                              {
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
                                            "$.vc.type"
                                        ],
                                        "filter": {
                                            "type": "string",
                                            "pattern": "ExampleDegreeCredential"
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
          """
              .trimIndent()
      val clientMetadata =
          """
          {
              "vp_formats": {
                  "jwt_vp": {
                      "alg": [
                          "EdDSA",
                          "ES256K"
                      ]
                  },
                  "ldp_vp": {
                      "proof_type": [
                          "Ed25519Signature2018"
                      ]
                  }
              }
          }
      """
              .trimIndent()
      val uri =
          Uri.parse("https://client.example.org/universal-link")
              .buildUpon()
              .appendQueryParameter("response_type", "vp_token")
              .appendQueryParameter("client_id", "https://client.example.org/callback")
              .appendQueryParameter("redirect_uri", "https://client.example.org/callback")
              .appendQueryParameter("presentation_definition", presentationDefinition)
              .appendQueryParameter("client_metadata", clientMetadata)
              .build()
      val registry = VerifiablePresentationApi.repository
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
      val signedValue = JoseUtils.sign(header, payloadValue, jwk)
      val payload = JoseUtils.parse(signedValue).payload()
      val record =
          VerifiableCredentialsRecord("1", "test", "type", "jwt_vc_json", signedValue, payload)
      registry.save("1", record)
      println(uri.toString())
      val interactor =
          object : VerifiablePresentationInteractor {
            override fun confirm(
                context: Context,
                viewData: VerifiablePresentationViewData,
                evaluation: PresentationDefinitionEvaluation,
                callback: VerifiablePresentationInteractorCallback
            ) {
              callback.accept()
            }
          }
      val result =
          VerifiablePresentationApi.handleVpRequest(
              context, "1", uri.toString(), interactor = interactor)
      result.onSuccess { print("success") }
      result.onFailure { print("failure") }
    }
  }

  @Test
  fun to_handle_vp_request_with_request_object() {
    runBlocking {
      val registry = VerifiablePresentationApi.repository
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
          VerifiableCredentialsRecord("1", "test", "type", "jwt_vc_json", signedValue, payload)
      registry.save("1", record)
      val vpPayload =
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
                                            "$.type",
                                            "$.vc.type"
                                        ],
                                        "filter": {
                                            "type": "string",
                                            "pattern": "ExampleDegreeCredential"
                                        }
                                    },
                                    {
                                        "path": [
                                            "$.credentialSubject.given_name",
                                            "$.vc.credentialSubject.given_name"
                                        ]
                                    },
                                    {
                                        "path": [
                                            "$.credentialSubject.family_name",
                                            "$.vc.credentialSubject.family_name"
                                        ]
                                    },
                                    {
                                        "path": [
                                            "$.credentialSubject.birthdate",
                                            "$.vc.credentialSubject.birthdate"
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
      val vpRequestSignedValue = JoseUtils.sign(header, vpPayload, jwk)
      val url = "openid4vp://?request=$vpRequestSignedValue&client_id=123"
      // eyJ0eXBlIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJyZXNwb25zZV90eXBlIjoidnBfdG9rZW4iLCJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6ImV4YW1wbGUgd2l0aCBzZWxlY3RpdmUgZGlzY2xvc3VyZSIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IklEIGNhcmQgd2l0aCBjb25zdHJhaW50cyIsImZvcm1hdCI6eyJsZHBfdmMiOnsicHJvb2ZfdHlwZSI6WyJFZDI1NTE5U2lnbmF0dXJlMjAxOCJdfX0sImNvbnN0cmFpbnRzIjp7ImxpbWl0X2Rpc2Nsb3N1cmUiOiJyZXF1aXJlZCIsImZpZWxkcyI6W3sicGF0aCI6WyIkLnR5cGUiLCIkLnZjLnR5cGUiXSwiZmlsdGVyIjp7InR5cGUiOiJzdHJpbmciLCJwYXR0ZXJuIjoiRXhhbXBsZURlZ3JlZUNyZWRlbnRpYWwifX0seyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuZ2l2ZW5fbmFtZSIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZ2l2ZW5fbmFtZSJdfSx7InBhdGgiOlsiJC5jcmVkZW50aWFsU3ViamVjdC5mYW1pbHlfbmFtZSIsIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZmFtaWx5X25hbWUiXX0seyJwYXRoIjpbIiQuY3JlZGVudGlhbFN1YmplY3QuYmlydGhkYXRlIiwiJC52Yy5jcmVkZW50aWFsU3ViamVjdC5iaXJ0aGRhdGUiXX1dfX1dfSwicmVkaXJlY3RfdXJpIjoiaHR0cHM6Ly9jbGllbnQuZXhhbXBsZS5vcmcvY2FsbGJhY2siLCJjbGllbnRfbWV0YWRhdGEiOnsiaXNzdWVyIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwLzEyMyIsImNsaWVudF9pZCI6InByaXZhdGVLZXlKd3QiLCJjbGllbnRfc2VjcmV0IjoicHJpdmF0ZUtleUp3dFNlY3JldCIsImNsaWVudF9pZF9pc3N1ZWRfYXQiOjI4OTMyNTY4MDAsImNsaWVudF9zZWNyZXRfZXhwaXJlc19hdCI6Mjg5MzI3NjgwMCwicmVkaXJlY3RfdXJpcyI6WyJodHRwczovL2NsaWVudC5leGFtcGxlLm9yZy9jYWxsYmFjayIsImh0dHBzOi8vY2xpZW50LmV4YW1wbGUub3JnL2NhbGxiYWNrMiIsImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MS9jYWxsYmFjayIsImh0dHBzOi8vd3d3LmNlcnRpZmljYXRpb24ub3BlbmlkLm5ldC90ZXN0L2EvaWRwX29pZGNfYmFzaWMvY2FsbGJhY2siLCJodHRwczovL2xvY2FsaG9zdC5lbW9iaXguY28udWs6ODQ0My90ZXN0L2EvaWRwX29pZGNfYmFzaWMvY2FsbGJhY2siLCJodHRwczovL2xvY2FsaG9zdC5lbW9iaXguY28udWs6ODQ0My90ZXN0L2EvaWRwX29pZGNfaW1wbGljaXQvY2FsbGJhY2siLCJodHRwczovL2xvY2FsaG9zdC5lbW9iaXguY28udWs6ODQ0My90ZXN0L2EvaWRwX29pZGNfaHlicmlkL2NhbGxiYWNrIl0sInJlc3BvbnNlX3R5cGVzIjpbImNvZGUiLCJ0b2tlbiIsImlkX3Rva2VuIiwiY29kZSB0b2tlbiIsImNvZGUgdG9rZW4gaWRfdG9rZW4iLCJ0b2tlbiBpZF90b2tlbiIsImNvZGUgaWRfdG9rZW4iLCJub25lIiwidnBfdG9rZW4iLCJ2cF90b2tlbiBpZF90b2tlbiJdLCJncmFudF90eXBlcyI6WyJhdXRob3JpemF0aW9uX2NvZGUiLCJyZWZyZXNoX3Rva2VuIiwicGFzc3dvcmQiLCJjbGllbnRfY3JlZGVudGlhbHMiXSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBhZGRyZXNzIHBob25lIG9mZmxpbmVfYWNjZXNzIGFjY291bnQgdHJhbnNmZXJzIHJlYWQgd3JpdGUiLCJjbGllbnRfbmFtZSI6Ik15IEV4YW1wbGUgQ2xpZW50IiwiY2xpZW50X25hbWUjamEtSnBhbi1KUCI6IuOCr-ODqeOCpOOCouODs-ODiOWQjSIsInRva2VuX2VuZHBvaW50X2F1dGhfbWV0aG9kIjoicHJpdmF0ZV9rZXlfand0IiwibG9nb191cmkiOiJodHRwczovL2NsaWVudC5leGFtcGxlLm9yZy9sb2dvLnBuZyIsImp3a3NfdXJpIjoiaHR0cHM6Ly9jbGllbnQuZXhhbXBsZS5vcmcvbXlfcHVibGljX2tleXMuandrcyIsImFwcGxpY2F0aW9uX3R5cGUiOiJ3ZWIiLCJqd2tzIjoie1xuICAgIFwia2V5c1wiOiBbXG4gICAgICAgIHtcbiAgICAgICAgICAgIFwia3R5XCI6IFwiUlNBXCIsXG4gICAgICAgICAgICBcImVcIjogXCJBUUFCXCIsXG4gICAgICAgICAgICBcInVzZVwiOiBcInNpZ1wiLFxuICAgICAgICAgICAgXCJraWRcIjogXCJjbGllbnRfc2VjcmV0X2tleVwiLFxuICAgICAgICAgICAgXCJhbGdcIjogXCJSUzI1NlwiLFxuICAgICAgICAgICAgXCJuXCI6IFwic0ZTcXNXdTJrb1U2OW9HNjdMNXdzVkd3emt5ZTgwQmQ5bG1PZmlTa1NUWHljOElLbDRnd21qOXRqenh4QTFwR1lpNFNLRVFhQllObDhKckdodHRCY2J0cmFxd2FTNVE2anBHMjRDMXo5bmpVdW1KV0puZUEzRUo5THB1bjlkM3VDQTNiXzcxWG5LNVByLVZ0d3pwVTZ6OFZHTk1aaGw4clo1cDFMMHN5TXBaMDN5NXRTV1ZNbnRjZWlxTmFGdUpDRlhHTVZTbHA2dnJWQ3FwY000cjAzNXRVUi1Qd2pTeW5weGU3T0dsUXBIVlN2QkNiWEpKdWZpMFF4SUlqZFB4MmthNTg2VGx2RmpWdTBRQkVjRW9uX0JNclBEV1BEMWFhQUVjU1BNOVU3Zld6bEs2YnRKOGQzN1RYWjBfclJQUV90VmVaQWxEblJjbGVoSGtLZmxrTmp3XCJcbiAgICAgICAgfSx7XG4gICAgXCJrdHlcIjogXCJSU0FcIixcbiAgICBcImVcIjogXCJBUUFCXCIsXG4gICAgXCJ1c2VcIjogXCJzaWdcIixcbiAgICBcImtpZFwiOiBcImNsaWVudF9zZWNyZXRfa2V5XzIwNDBcIixcbiAgICBcImFsZ1wiOiBcIlJTMjU2XCIsXG4gICAgXCJuXCI6IFwidG5kSVBpWW5UdzhVbGJ2VTRHU0Q3N3RQd2VQTnB1MVZZbVRXbXJPQTFldHZOVjd4ZVUxMGxIUHNjbWJyNzAyYmJvNWFkRVZ3V3Nxd0hrUzBsdnRCQmVwYjNCS2hGd2w4NF9GZnFwLVBfcnFsZHVRM1hucmk1QmZlc3JlT3k2blpRY1E5NU9TUjBNNEhZZ2ZocnNNWEN4UXNBMUdDRENJN29pS200M2ljVHhhUG9IMjMycWZKRkdfckluSEJFb2tPLUJDS18wQ3QtdG82ZHlSTHhsRGdDb0tSNExuV0pfRVRmeHpiOUxTZHA2bU8zY2NEOXI5UWl0N3RnYnYtdkJ6R3RNVzlZZF9pd2JITW8ycVJSTW5iUU1ISEszVmlwMnhDRUlpMXYzSEMtLVVVWHVlY0pfU1lHMkQ2OVVLQzNodk81NGxqV0tzSXllR0FrY1R5RFpFUlwiXG59ICAgIF1cbn0iLCJhdXRob3JpemF0aW9uX2RldGFpbHNfdHlwZXMiOlsicGF5bWVudF9pbml0aWF0aW9uIiwiYWNjb3VudF9pbmZvcm1hdGlvbiIsIm9wZW5pZF9jcmVkZW50aWFsIl19fQ.F3THcbcpl_66iGYlyG5A4jgCXr8lH6tBDZ0etSfaG8Jl5k3pDp2Vl970ZugNcuFaJJ45W0Pf5ydhQMu42Kngpw
      print(url)
      val interactor =
          object : VerifiablePresentationInteractor {
            override fun confirm(
                context: Context,
                viewData: VerifiablePresentationViewData,
                evaluation: PresentationDefinitionEvaluation,
                callback: VerifiablePresentationInteractorCallback
            ) {
              callback.accept()
            }
          }
      val response =
          VerifiablePresentationApi.handleVpRequest(context, "1", url, interactor = interactor)

    }
  }
}
