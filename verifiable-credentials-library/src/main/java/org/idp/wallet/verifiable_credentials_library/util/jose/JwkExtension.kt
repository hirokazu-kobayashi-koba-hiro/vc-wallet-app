package org.idp.wallet.verifiable_credentials_library.util.jose

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

fun JWK.transformPublicKey(): PublicKey {
  if (this.algorithm.name.contains("EC", true)) {
    return toEcPublicKey()
  }
  if (this.algorithm.name.contains("RS", true)) {
    return toRsaPublicKey()
  }
  throw RuntimeException("failed transformation to public key")
}

fun JWK.toEcPublicKey(): ECPublicKey {
  return toECKey().toECPublicKey()
}

fun JWK.toRsaPublicKey(): RSAPublicKey {
  return toRSAKey().toRSAPublicKey()
}

fun JWK.toJWSAlgorithm(): JWSAlgorithm {
  val algorithm = this.algorithm
  if (algorithm != null) {
    return JWSAlgorithm(algorithm.name)
  }
  return when (keyType.value) {
    "EC" -> JWSAlgorithm("ES256")
    "RSA" -> JWSAlgorithm("RS256")
    else -> throw RuntimeException("unsupported key type: ${keyType.value}")
  }
}
