package org.idp.wallet.verifiable_credentials_library.domain.type.oauth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Long,
    val scope: String?,
    val cNonce: String?
)
