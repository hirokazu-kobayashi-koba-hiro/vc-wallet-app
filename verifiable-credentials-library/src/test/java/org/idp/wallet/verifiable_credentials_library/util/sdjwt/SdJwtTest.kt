package org.idp.wallet.verifiable_credentials_library.util.sdjwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.*
import com.nimbusds.jose.jwk.JWK
import eu.europa.ec.eudi.sdjwt.*
import eu.europa.ec.eudi.sdjwt.SdJwtIssuer
import eu.europa.ec.eudi.sdjwt.nimbus
import kotlinx.coroutines.runBlocking
import org.idp.wallet.verifiable_credentials_library.util.jose.toRsaPublicKey
import org.junit.Test

class SdJwtTest {

  @Test
  fun `create and verify`() = runBlocking {
    val issuerKeyPair =
        """
                {
                  "p": "5SlZ67NYmHkgalvjbBfRjUzO_Vin96cvlYnfOQQC1Z5B5MjfAZvoCWm9evGVfQnIl8GxTs-COpWhy-M6V2zYuhY-LszQPCXwrZp3AyAqhHD3pg9gkdm8UyBjsg10yLpW_5VZ4mBy_uziJgFcphYS5AZXv-LdhqeJZmui6QaAZH8",
                  "kty": "RSA",
                  "q": "1wPF3KVtxv680kZPiCGrDwZ9-GPlMq-kJZ2Suy9ms3Nf3EKXkl9GOLYzMATcAo-NWwRofnr0daCeQWVpOSs1-AszeFNhTC4PIqMOh_5BbXwH025--rFLPvxq9a_s4vRSBe7oWUHmJbK1LqKhWxK0Z9fZr4OSf7RR-5q1IO7ZG7E",
                  "d": "SrWXfApOyq1Lx1lYkHMZKHWIqr6wUqvg8zmX17qYezLfqk5-p_PxlpHAXLyQQ426h7d7IrOshE9hBmD1aQz5Wk5Stkz20MXactHzZrH0KY7wQfbPcomJ5yQ6TqGJ7P_q3jWAyWlw0-7THEmBKELBHAAiqmAVbrqY-eFVASAgjJmbW-aImYcpzdpoDIhgXod0E0lN6wLQJUwsMYxR5pNjvpRammynhVPLAaT-40m3QXitU44g9P4WihreAD6oO-YkzuOEb9rx93YH9JVdgiNqDYWjfjIB4aXwiYQFFqBaiB9AZsk53oFu8wq4USmozp9cPlDyJw0bFk93GuQ5vm6LEQ",
                  "e": "AQAB",
                  "qi": "Gy6HxwBD2I84yuPw3XPeYPrPk3k-wR_tEQ3gDASromzmjsNjqBx2O5VUamyUir7C-tWLMPeFK5lB6dx25dHEU_yqzw352qPYVeyUdcpmwMZIz75jBGkEAhTxIBLfdWxFE-MTChZE498J-wCM6KfAmyy4xxggvH5C5NbtB4VDzDs",
                  "dp": "RmGM3HcYnruU2RWfo3MtSNhuOiEgdU9DrgnKJRi2RiV0129aDoRzn3B6YWgRIXX_xZwXeL0CojuSlawUHwWvCngKYRVGXzLH2Im4Q519cW_An5_ZaPIWhPFNrUG0vHF-hsC3-m7k4qaeTk3cxqXA54eLK8PhhZmu-WXVgvwcMsE",
                  "dq": "tX6hzhZ2C7uLd5OSHr8MvaolOz5XgelxDa9s5ESm1GNXT32GpC93_vcNTkNL032JPIMEEX-ISqJB88G-iFtJOw95auA3CS7356zr3y9n2xsPIBHtbX-qnIhFYQ4XT5wcgJWh23Yc50VqWus8eLvZPK6fOeA-ET4M1e1JZ3s2GIE",
                  "n": "wHkbH1c0FVsIqobrmB8NTfQlf9XDpJCwYj7G1m1Xhm01JF2zZCSLxFEe_tOs5ZvCKhNXLUI0cM-FxDZw-5x0v9Owzn_eKfYesLqZcclQRswyRCFnpG6kdEuCx1Nv2yVb_yNl9RVaIysbfVJee9SPRXnKPGYFjZvQpWLtjZ6nEj-YS4PJ218UYTLFPWHdLFZHpgqZKmq1p3bGSVliJf2P_gOaazwmHi9F5d-yMjYhHBhP_oa798Hfeh4O-bzdPPD9kR3gqZbN_JPm3oVB7qJpGND4qtY-_tQMLVjfjF3iIWl4iuwKblu2vW0RTfwAaEkrWXzhfB3wOSx2rHHwSwXgzw"
                }

            """
            .trimIndent()
    val jwk = JWK.parse(issuerKeyPair)
    val rsaKey = jwk.toRSAKey()
    val plainPayload =
        listOf(
            SdJwtElement(
                "@context",
                listOf("https://www.w3.org/2018/credentials/v1", "https://w3id.org/vaccination/v1"),
                true),
            SdJwtElement("type", listOf("VerifiableCredential", "VaccinationCertificate"), true),
            SdJwtElement("issuer", "https://example.com/issuer", true),
            SdJwtElement("issuanceDate", "2023-02-09T11:01:59Z", true),
            SdJwtElement("expirationDate", "2028-02-08T11:01:59Z", true),
            SdJwtElement("name", "Vaccination Certificate", false),
            SdJwtElement("description", "Vaccination Certificate", false),
            SdJwtElement(
                "cnf",
                mapOf(
                    "jwk" to
                        mapOf(
                            "kty" to "EC",
                            "crv" to "P-256",
                            "x" to "TCAER19Zvu3OHF4j4W4vfSVoHIP1ILilDls7vCeGemc",
                            "y" to "ZxjiWWbZMQGHVWKVQ4hbSIirsVfuecCE6t4jT9F2HZQ")),
                true))

    val structuredPayload =
        mapOf(
            "credentialSubject" to
                listOf(
                    SdJwtElement("type", "VaccinationEvent", false),
                ),
            "vaccine" to
                listOf(
                    SdJwtElement("type", "Vaccine", false),
                ),
            "recipient" to
                listOf(
                    SdJwtElement("type", "VaccineRecipient", false),
                ))

    val payload = SdJwtPayload(plainPayload, structuredPayload)
    val sdObject: SdObject = SdObjectCreator.create(payload)
    val issuer =
        SdJwtIssuer.nimbus(signer = RSASSASigner(rsaKey), signAlgorithm = JWSAlgorithm.RS256)
    val sdkJwt = issuer.issue(sdObject).getOrThrow()
    val rawJwt = sdkJwt.serialize()
    println(rawJwt)

    val publicKey = jwk.toRsaPublicKey()
    val jwtSignatureVerifier = RSASSAVerifier(publicKey).asJwtVerifier()
    val verifiedSdJwt = SdJwtVerifier.verifyIssuance(jwtSignatureVerifier, rawJwt).getOrThrow()
    println(verifiedSdJwt.jwt.second)
    val recreateClaims = verifiedSdJwt.recreateClaims(claimsOf = { jwt -> jwt.second })
    print(recreateClaims)
  }
}
