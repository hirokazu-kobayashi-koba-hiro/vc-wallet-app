package org.idp.wallet.verifiable_credentials_library.basic.jose

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT

object JoseHandler {

  fun parse(jose: String): JwtObject {
    val parsedJwt = JWTParser.parse(jose)
    return JwtObject(parsedJwt)
  }

  fun sign(header: Map<String, Any>, payload: Map<String, Any>, jwkValue: String): String {
    val jwk = JWK.parse(jwkValue)
    val headers =
        JWSHeader.Builder(JWSAlgorithm.parse(jwk.algorithm.name)).customParams(header).build()
    val claimSet = JWTClaimsSet.parse(payload)
    val jws = SignedJWT(headers, claimSet)
    val jwsSigner = DefaultJWSSignerFactory().createJWSSigner(jwk)
    jws.sign(jwsSigner)
    return jws.serialize()
  }

  fun sign(header: Map<String, Any>, payload: String, jwkValue: String): String {
    val jwk = JWK.parse(jwkValue)
    val headers =
        JWSHeader.Builder(JWSAlgorithm.parse(jwk.algorithm.name)).customParams(header).build()
    val claimSet = JWTClaimsSet.parse(payload)
    val jws = SignedJWT(headers, claimSet)
    val jwsSigner = DefaultJWSSignerFactory().createJWSSigner(jwk)
    jws.sign(jwsSigner)
    return jws.serialize()
  }

  fun generateECKey(keyId: String): String {
    val ecJWK: ECKey = ECKeyGenerator(Curve.P_256).keyID(keyId).generate()
    return ecJWK.toJSONString()
  }
}

class JwtObject(private val jwt: JWT) {

  fun kid(): String {
    return jwt.header.customParams.getOrDefault("kid", "") as String
  }

  fun algorithm(): String {
    return jwt.header.algorithm.name
  }

  fun payload(): Map<String, Any> {
    return jwt.jwtClaimsSet.claims
  }

  fun valueAsStringFromPayload(key: String): String? {
    if (containsKey(key)) {
      return jwt.jwtClaimsSet.getStringClaim(key)
    }
    return null
  }

  fun valueAsObjectFromPayload(key: String): Map<String, Any>? {
    if (containsKey(key)) {
      return jwt.jwtClaimsSet.getJSONObjectClaim(key)
    }
    return null
  }

  fun containsKey(key: String): Boolean {
    return jwt.jwtClaimsSet.claims.containsKey(key)
  }
}
