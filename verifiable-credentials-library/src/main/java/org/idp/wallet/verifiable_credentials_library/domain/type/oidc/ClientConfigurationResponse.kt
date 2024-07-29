package org.idp.wallet.verifiable_credentials_library.domain.type.oidc

data class ClientConfigurationResponse(
    val clientId: String,
    val clientSecret: String,
    val registrationAccessToken: String?,
    val registrationClientUrl: String?,
    val clientIdIssuedAt: Long?,
    val clientSecretExpiresAt: Long?,
)
