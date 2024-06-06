package org.idp.wallet.verifiable_credentials_library.domain.type.oauth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Int,
    val scope: String?,
    val cNonce: String?
)
