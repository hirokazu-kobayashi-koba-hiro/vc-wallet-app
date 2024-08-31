package org.idp.wallet.verifiable_credentials_library.domain.configuration

import java.util.Arrays
import java.util.Objects
import java.util.stream.Collectors

class ClientConfiguration(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUris: List<String> = ArrayList(),
    val tokenEndpointAuthMethod: String = "",
    val grantTypes: List<String> = ArrayList(),
    val responseTypes: List<String> = ArrayList(),
    val clientName: String = "",
    val clientUri: String = "",
    val logoUri: String = "",
    val scope: String = "",
    val contacts: String = "",
    val tosUri: String = "",
    val policyUri: String = "",
    val jwksUri: String? = null,
    val jwks: String? = null,
    val softwareId: String = "",
    val softwareVersion: String = "",
    val requestUris: List<String> = ArrayList(),
    val backchannelTokenDeliveryMode: String = "",
    val backchannelClientNotificationEndpoint: String = "",
    val backchannelAuthenticationRequestSigningAlg: String = "",
    val backchannelUserCodeParameter: Boolean? = null,
    val applicationType: String = "web",
    val idTokenEncryptedResponseAlg: String? = null,
    val idTokenEncryptedResponseEnc: String? = null,
    val authorizationDetailsTypes: List<String> = ArrayList(),
    val tlsClientAuthSubjectDn: String? = null,
    val tlsClientAuthSanDns: String? = null,
    val tlsClientAuthSanUri: String? = null,
    val tlsClientAuthSanIp: String? = null,
    val tlsClientAuthSanEmail: String? = null,
    val tlsClientCertificateBoundAccessTokens: Boolean = false,
    val authorizationSignedResponseAlg: String? = null,
    val authorizationEncryptedResponseAlg: String? = null,
    val authorizationEncryptedResponseEnc: String? = null,
    // extension
    val supportedJar: Boolean = false,
    val issuer: String? = null
) {
  fun scopes(): List<String> {
    return if (Objects.isNull(scope)) {
      listOf()
    } else
        Arrays.stream(scope.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .collect(Collectors.toList())
  }

  fun filteredScope(spacedScopes: String): Set<String> {
    if (Objects.isNull(spacedScopes) || spacedScopes.isEmpty()) {
      return setOf()
    }
    val scopes =
        Arrays.stream(
                spacedScopes.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .collect(Collectors.toList())
    return scopes
        .stream()
        .filter { scope: String -> scopes().contains(scope) }
        .collect(Collectors.toSet())
  }

  fun filteredScope(scopes: List<String>): Set<String>? {
    return scopes
        .stream()
        .filter { scope: String -> scopes().contains(scope) }
        .collect(Collectors.toSet())
  }

  fun isSupportedJar(): Boolean {
    return supportedJar
  }

  fun isRegisteredRequestUri(requestUri: String): Boolean {
    return requestUris.contains(requestUri)
  }

  fun isRegisteredRedirectUri(redirectUri: String): Boolean {
    return redirectUris.contains(redirectUri)
  }

  fun tokenIssuer(): String? {
    return issuer
  }

  fun isSupportedResponseType(responseType: String): Boolean {
    return responseTypes.contains(responseType)
  }

  fun matchClientSecret(that: String): Boolean {
    return clientSecret == that
  }

  fun clientAuthenticationType(): String {
    return tokenEndpointAuthMethod
  }

  fun backchannelTokenDeliveryMode(): String {
    return backchannelTokenDeliveryMode
  }

  fun hasBackchannelTokenDeliveryMode(): Boolean {
    return backchannelTokenDeliveryMode().isNotEmpty()
  }

  fun backchannelClientNotificationEndpoint(): String? {
    return backchannelClientNotificationEndpoint
  }

  fun hasBackchannelClientNotificationEndpoint(): Boolean {
    return !backchannelClientNotificationEndpoint.isEmpty()
  }

  fun backchannelAuthenticationRequestSigningAlg(): String? {
    return backchannelAuthenticationRequestSigningAlg
  }

  fun hasBackchannelAuthenticationRequestSigningAlg(): Boolean {
    return !backchannelAuthenticationRequestSigningAlg.isEmpty()
  }

  fun backchannelUserCodeParameter(): Boolean? {
    return backchannelUserCodeParameter
  }

  fun hasBackchannelUserCodeParameter(): Boolean {
    return Objects.nonNull(backchannelUserCodeParameter)
  }

  fun isSupportedGrantType(grantType: String): Boolean {
    return grantTypes.contains(grantType)
  }

  fun isWebApplication(): Boolean {
    return applicationType == "web"
  }

  fun idTokenEncryptedResponseAlg(): String? {
    return idTokenEncryptedResponseAlg
  }

  fun idTokenEncryptedResponseEnc(): String? {
    return idTokenEncryptedResponseEnc
  }

  fun hasEncryptedIdTokenMeta(): Boolean {
    return (Objects.nonNull(idTokenEncryptedResponseAlg) &&
        Objects.nonNull(idTokenEncryptedResponseEnc))
  }

  fun authorizationDetailsTypes(): List<String>? {
    return authorizationDetailsTypes
  }

  fun isAuthorizedAuthorizationDetailsType(type: String): Boolean {
    return authorizationDetailsTypes.contains(type)
  }

  fun tlsClientAuthSubjectDn(): String? {
    return tlsClientAuthSubjectDn
  }

  fun tlsClientAuthSanDns(): String? {
    return tlsClientAuthSanDns
  }

  fun tlsClientAuthSanUri(): String? {
    return tlsClientAuthSanUri
  }

  fun tlsClientAuthSanIp(): String? {
    return tlsClientAuthSanIp
  }

  fun tlsClientAuthSanEmail(): String? {
    return tlsClientAuthSanEmail
  }

  fun isTlsClientCertificateBoundAccessTokens(): Boolean {
    return tlsClientCertificateBoundAccessTokens
  }

  fun authorizationSignedResponseAlg(): String? {
    return authorizationSignedResponseAlg
  }

  fun hasAuthorizationSignedResponseAlg(): Boolean {
    return Objects.nonNull(authorizationSignedResponseAlg)
  }

  fun authorizationEncryptedResponseAlg(): String? {
    return authorizationEncryptedResponseAlg
  }

  fun authorizationEncryptedResponseEnc(): String? {
    return authorizationEncryptedResponseEnc
  }

  fun hasEncryptedAuthorizationResponseMeta(): Boolean {
    return (Objects.nonNull(idTokenEncryptedResponseAlg) &&
        Objects.nonNull(idTokenEncryptedResponseEnc))
  }
}
