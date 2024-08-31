package org.idp.wallet.verifiable_credentials_library.domain.type.vc

data class JwtVcConfiguration(val issuer: String, val jwksUri: String?, val jwks: String?)
