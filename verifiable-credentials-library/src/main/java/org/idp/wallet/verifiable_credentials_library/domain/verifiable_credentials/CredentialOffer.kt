package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

class CredentialOffer(
    val credentialIssuer: String,
    val credentialConfigurationIds: List<String>,
    val preAuthorizedCodeGrant: PreAuthorizedCodeGrant? = null,
    val authorizedCodeGrant: AuthorizedCodeGrant? = null
) {

  fun credentialIssuerMetadataEndpoint(): String {
    return "$credentialIssuer/.well-known/openid-credential-issuer"
  }

  fun oiddEndpoint(): String {
    return "$credentialIssuer/.well-known/openid-configuration"
  }
}

data class PreAuthorizedCodeGrant(
    val preAuthorizedCode: String,
    val length: Int? = null,
    val inputMode: String? = null,
    val description: String? = null,
)

data class AuthorizedCodeGrant(
    val issuerState: String? = null,
    val authorizationServer: String? = null,
)
