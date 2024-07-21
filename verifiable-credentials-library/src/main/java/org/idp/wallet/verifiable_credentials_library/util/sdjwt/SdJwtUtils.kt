package org.idp.wallet.verifiable_credentials_library.util.sdjwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import eu.europa.ec.eudi.sdjwt.SdJwt
import eu.europa.ec.eudi.sdjwt.SdJwtIssuer
import eu.europa.ec.eudi.sdjwt.SdJwtVerifier
import eu.europa.ec.eudi.sdjwt.SdObject
import eu.europa.ec.eudi.sdjwt.asJwtVerifier
import eu.europa.ec.eudi.sdjwt.nimbus
import eu.europa.ec.eudi.sdjwt.recreateClaims
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.jose.toEcPublicKey
import org.idp.wallet.verifiable_credentials_library.util.jose.toJWSAlgorithm
import org.idp.wallet.verifiable_credentials_library.util.jose.toRsaPublicKey

object SdJwtUtils {

  fun issue(payload: SdJwtPayload, privateKey: String): SdJwt.Issuance<SignedJWT> {
    val sdObject: SdObject = SdObjectCreator.create(payload)
    val pair = toSigner(privateKey)
    val issuer = SdJwtIssuer.nimbus(signer = pair.second, signAlgorithm = pair.first)
    return issuer.issue(sdObject).getOrThrow()
  }

  private fun toSigner(privateKey: String): Pair<JWSAlgorithm, JWSSigner> {
    val jwk = JWK.parse(privateKey)
    val keyType = jwk.keyType
    val jwsAlgorithm = jwk.toJWSAlgorithm()
    if (keyType.value == "EC") {
      val ecKey = jwk.toECKey()
      return Pair(jwsAlgorithm, ECDSASigner(ecKey))
    }
    if (keyType.value == "RSA") {
      val rsaKey = jwk.toRSAKey()
      return Pair(jwsAlgorithm, RSASSASigner(rsaKey))
    }
    throw RuntimeException("unsupported key type: ${keyType.value}")
  }

  suspend fun parseAndVerifySignature(sdJwt: String, jwks: String): Map<String, Any> {
    val jwkSet = JWKSet.parse(jwks)
    val signedJWT = SignedJWT.parse(sdJwt)
    val jwk = JoseUtils.findKey(signedJWT, jwkSet)
    if (signedJWT.header.algorithm.name.contains("ES", true)) {
      val publicKey = jwk.toEcPublicKey()
      val jwtSignatureVerifier = ECDSAVerifier(publicKey).asJwtVerifier()
      val verifiedSdJwt = SdJwtVerifier.verifyIssuance(jwtSignatureVerifier, sdJwt).getOrThrow()
      return verifiedSdJwt.recreateClaims(claimsOf = { jwt -> jwt.second })
    }
    if (signedJWT.header.algorithm.name.contains("RS", true)) {
      val publicKey = jwk.toRsaPublicKey()
      val jwtSignatureVerifier = RSASSAVerifier(publicKey).asJwtVerifier()
      val verifiedSdJwt = SdJwtVerifier.verifyIssuance(jwtSignatureVerifier, sdJwt).getOrThrow()
      return verifiedSdJwt.recreateClaims(claimsOf = { jwt -> jwt.second })
    }
    throw RuntimeException("unsupported algorithm: ${signedJWT.header.algorithm.name}")
  }
}
