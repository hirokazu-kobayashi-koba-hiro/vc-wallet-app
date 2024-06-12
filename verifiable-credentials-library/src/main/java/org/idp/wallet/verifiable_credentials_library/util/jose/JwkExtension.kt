package org.idp.wallet.verifiable_credentials_library.util.jose

import com.nimbusds.jose.jwk.JWK
import java.security.PublicKey

fun JWK.transformPublicKey(): PublicKey {
  if (this.algorithm.name.contains("EC", true)) {
    return toECKey().toECPublicKey()
  }
  if (this.algorithm.name.contains("RS", true)) {
    return toRSAKey().toRSAPublicKey()
  }
  throw RuntimeException("failed transformation to public key")
}
