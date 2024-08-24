package org.idp.wallet.verifiable_credentials_library.domain.type.oauth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val idToken: String? = null,
    val expiresIn: Long,
    val scope: String? = null,
    val cNonce: String? = null,
    val cNonceExpiresIn: Int? = null
)
