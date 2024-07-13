package org.idp.wallet.verifiable_credentials_library.util.jose

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
