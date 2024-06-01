package org.idp.wallet.verifiable_credentials_library.type.vc

data class CredentialIssuerMetadata(
    val credentialIssuer: String,
    val authorizationServers: List<String>?,
    val credentialEndpoint: String,
    val batchCredentialEndpoint: String?,
    val deferredCredentialEndpoint: String?,
    val notificationEndpoint: String?,
    val credentialResponseEncryption: CredentialResponseEncryption?,
    val credentialIdentifiersSupported: Boolean = false,
    val signedMetadata: String?,
    val display: List<Display>?,
    val credentialConfigurationsSupported: Map<String, CredentialConfiguration>
) {

  fun findCredentialConfiguration(credential: String): CredentialConfiguration? {
    return credentialConfigurationsSupported[credential]
  }

  fun getOpenIdConfigurationEndpoint(): String {
    authorizationServers?.let {
      if (it.isNotEmpty()) {
        return "${it[0]}/.well-known/openid-configuration"
      }
    }
    return "$credentialIssuer/.well-known/openid-configuration"
  }
}

data class CredentialResponseEncryption(
    val algValuesSupported: List<String>,
    val encValuesSupported: List<String>,
    val encryptionRequired: Boolean
) {}

data class Display(
    val name: String?,
    val locale: String?,
    val logo: Logo?,
    val description: String?,
    val backgroundColor: String?,
    val backgroundImage: BackgroundImage?,
    val textColor: String?
) {}

data class BackgroundImage(val uri: String)

data class Logo(val uri: String, val altText: String?) {}

data class CredentialConfiguration(
    val format: String,
    val scope: String?,
    val cryptographicBindingMethodsSupported: List<String>?,
    val proofTypesSupported: ProofTypesSupported?,
    val display: List<Display>?,
) {
  fun getFirstDisplay(): Display? {
    return display?.get(0)
  }

  fun getFirstLogo(): Logo? {
    return display?.get(0)?.logo
  }

  fun findLogo(name: String): Logo? {
    return display?.find { it.name == name }?.logo
  }
}

data class ProofTypesSupported(val proofSigningAlgValuesSupported: List<String>) {}
