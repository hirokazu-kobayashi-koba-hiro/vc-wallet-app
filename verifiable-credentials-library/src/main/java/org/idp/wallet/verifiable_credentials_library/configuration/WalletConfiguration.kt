package org.idp.wallet.verifiable_credentials_library.configuration

import java.util.Arrays
import java.util.Objects
import java.util.stream.Collectors
import org.idp.wallet.verifiable_credentials_library.type.ResponseType

class WalletConfiguration(
    val issuer: String = "",
    val authorizationEndpoint: String? = null,
    val tokenEndpoint: String = "",
    val userinfoEndpoint: String = "",
    var jwks: String = "",
    val jwksUri: String? = null,
    val registrationEndpoint: String = "",
    val scopesSupported: List<String> = ArrayList(),
    val responseTypesSupported: List<String> = ArrayList(),
    val responseModesSupported: List<String> = ArrayList(),
    val grantTypesSupported: List<String> = ArrayList(),
    val acrValuesSupported: List<String> = ArrayList(),
    val subjectTypesSupported: List<String> = ArrayList(),
    val idTokenSigningAlgValuesSupported: List<String> = ArrayList(),
    val idTokenEncryptionAlgValuesSupported: List<String> = ArrayList(),
    val idTokenEncryptionEncValuesSupported: List<String> = ArrayList(),
    val userinfoSigningAlgValuesSupported: List<String> = ArrayList(),
    val userinfoEncryptionAlgValuesSupported: List<String> = ArrayList(),
    val userinfoEncryptionEncValuesSupported: List<String> = ArrayList(),
    val requestObjectSigningAlgValuesSupported: List<String> = ArrayList(),
    val requestObjectEncryptionAlgValuesSupported: List<String> = ArrayList(),
    val requestObjectEncryptionEncValuesSupported: List<String> = ArrayList(),
    val authorizationSigningAlgValuesSupported: List<String> = ArrayList(),
    val authorizationEncryptionAlgValuesSupported: List<String> = ArrayList(),
    val authorizationEncryptionEncValuesSupported: List<String> = ArrayList(),
    val tokenEndpointAuthMethodsSupported: List<String> = ArrayList(),
    val tokenEndpointAuthSigningAlgValuesSupported: List<String> = ArrayList(),
    val displayValuesSupported: List<String> = ArrayList(),
    val claimTypesSupported: List<String> = ArrayList(),
    val claimsSupported: List<String> = ArrayList(),
    val claimsParameterSupported: Boolean = true,
    val requestParameterSupported: Boolean = true,
    val requestUriParameterSupported: Boolean = true,
    val requireRequestUriRegistration: Boolean = true,
    val revocationEndpoint: String = "",
    val revocationEndpointAuthMethodsSupported: List<String> = ArrayList(),
    val revocationEndpointAuthSigningAlgValuesSupported: List<String> = ArrayList(),
    val introspectionEndpoint: String = "",
    val introspectionEndpointAuthMethodsSupported: List<String> = ArrayList(),
    val introspectionEndpointAuthSigningAlgValuesSupported: List<String> = ArrayList(),
    val codeChallengeMethodsSupported: List<String> = ArrayList(),
    val tlsClientCertificateBoundAccessTokens: Boolean = false,
    val requireSignedRequestObject: Boolean = false,
    val authorizationResponseIssParameterSupported: Boolean = false,
    val backchannelTokenDeliveryModesSupported: List<String> = ArrayList(),
    val backchannelAuthenticationEndpoint: String = "",
    val backchannelAuthenticationRequestSigningAlgValuesSupported: List<String> = ArrayList(),
    val backchannelUserCodeParameterSupported: Boolean? = null,
    val authorizationDetailsTypesSupported: List<String> = ArrayList(),
    // extension
    val fapiBaselineScopes: List<String> = ArrayList(),
    val fapiAdvanceScopes: List<String> = ArrayList(),
    val authorizationCodeValidDuration: Long = 600,
    val tokenSignedKeyId: String = "",
    val idTokenSignedKeyId: String = "",
    val accessTokenDuration: Long = 1800,
    val refreshTokenDuration: Long = 3600,
    val idTokenDuration: Long = 3600,
    val idTokenStrictMode: Boolean = false,
    val defaultMaxAge: Long = 86400,
    val authorizationResponseDuration: Long = 60
) {

  fun issuer(): String {
    return issuer
  }

  fun authorizationEndpoint(): String? {
    return authorizationEndpoint
  }

  fun tokenEndpoint(): String? {
    return tokenEndpoint
  }

  fun userinfoEndpoint(): String? {
    return userinfoEndpoint
  }

  fun jwks(): String? {
    return jwks
  }

  fun jwksUri(): String? {
    return jwksUri
  }

  fun registrationEndpoint(): String? {
    return registrationEndpoint
  }

  fun scopesSupported(): List<String>? {
    return scopesSupported
  }

  fun responseTypesSupported(): List<String>? {
    return responseTypesSupported
  }

  fun responseModesSupported(): List<String>? {
    return responseModesSupported
  }

  fun grantTypesSupported(): List<String>? {
    return grantTypesSupported
  }

  fun acrValuesSupported(): List<String>? {
    return acrValuesSupported
  }

  fun subjectTypesSupported(): List<String>? {
    return subjectTypesSupported
  }

  fun idTokenSigningAlgValuesSupported(): List<String>? {
    return idTokenSigningAlgValuesSupported
  }

  fun idTokenEncryptionAlgValuesSupported(): List<String>? {
    return idTokenEncryptionAlgValuesSupported
  }

  fun idTokenEncryptionEncValuesSupported(): List<String>? {
    return idTokenEncryptionEncValuesSupported
  }

  fun userinfoSigningAlgValuesSupported(): List<String>? {
    return userinfoSigningAlgValuesSupported
  }

  fun userinfoEncryptionAlgValuesSupported(): List<String>? {
    return userinfoEncryptionAlgValuesSupported
  }

  fun userinfoEncryptionEncValuesSupported(): List<String>? {
    return userinfoEncryptionEncValuesSupported
  }

  fun requestObjectSigningAlgValuesSupported(): List<String>? {
    return requestObjectSigningAlgValuesSupported
  }

  fun requestObjectEncryptionAlgValuesSupported(): List<String>? {
    return requestObjectEncryptionAlgValuesSupported
  }

  fun requestObjectEncryptionEncValuesSupported(): List<String>? {
    return requestObjectEncryptionEncValuesSupported
  }

  fun authorizationSigningAlgValuesSupported(): List<String>? {
    return authorizationSigningAlgValuesSupported
  }

  fun authorizationEncryptionAlgValuesSupported(): List<String>? {
    return authorizationEncryptionAlgValuesSupported
  }

  fun authorizationEncryptionEncValuesSupported(): List<String>? {
    return authorizationEncryptionEncValuesSupported
  }

  fun tokenEndpointAuthMethodsSupported(): List<String>? {
    return tokenEndpointAuthMethodsSupported
  }

  fun isSupportedClientAuthenticationType(value: String): Boolean {
    return tokenEndpointAuthMethodsSupported.contains(value)
  }

  fun tokenEndpointAuthSigningAlgValuesSupported(): List<String>? {
    return tokenEndpointAuthSigningAlgValuesSupported
  }

  fun displayValuesSupported(): List<String>? {
    return displayValuesSupported
  }

  fun claimTypesSupported(): List<String>? {
    return claimTypesSupported
  }

  fun claimsSupported(): List<String>? {
    return claimsSupported
  }

  fun claimsParameterSupported(): Boolean {
    return claimsParameterSupported
  }

  fun requestParameterSupported(): Boolean {
    return requestParameterSupported
  }

  fun requestUriParameterSupported(): Boolean {
    return requestUriParameterSupported
  }

  fun requireRequestUriRegistration(): Boolean {
    return requireRequestUriRegistration
  }

  fun revocationEndpoint(): String? {
    return revocationEndpoint
  }

  fun revocationEndpointAuthMethodsSupported(): List<String>? {
    return revocationEndpointAuthMethodsSupported
  }

  fun revocationEndpointAuthSigningAlgValuesSupported(): List<String>? {
    return revocationEndpointAuthSigningAlgValuesSupported
  }

  fun introspectionEndpoint(): String? {
    return introspectionEndpoint
  }

  fun introspectionEndpointAuthMethodsSupported(): List<String>? {
    return introspectionEndpointAuthMethodsSupported
  }

  fun introspectionEndpointAuthSigningAlgValuesSupported(): List<String>? {
    return introspectionEndpointAuthSigningAlgValuesSupported
  }

  fun codeChallengeMethodsSupported(): List<String>? {
    return codeChallengeMethodsSupported
  }

  fun isTlsClientCertificateBoundAccessTokens(): Boolean {
    return tlsClientCertificateBoundAccessTokens
  }

  fun requireSignedRequestObject(): Boolean {
    return requireSignedRequestObject
  }

  fun authorizationResponseIssParameterSupported(): Boolean {
    return authorizationResponseIssParameterSupported
  }

  fun filteredScope(spacedScopes: String): List<String> {
    val scopes =
        Arrays.stream(
                spacedScopes.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .collect(Collectors.toList())
    return scopes
        .stream()
        .filter { scope: String -> scopesSupported.contains(scope) }
        .collect(Collectors.toList())
  }

  fun filteredScope(scopes: List<String>): List<String> {
    return scopes
        .stream()
        .filter { scope: String -> scopesSupported.contains(scope) }
        .collect(Collectors.toList())
  }

  fun hasFapiBaselineScope(scopes: Set<String>): Boolean {
    return scopes.stream().anyMatch { scope: String -> fapiBaselineScopes.contains(scope) }
  }

  fun hasFapiAdvanceScope(scopes: Set<String>): Boolean {
    return scopes.stream().anyMatch { scope: String -> fapiAdvanceScopes.contains(scope) }
  }

  fun authorizationCodeValidDuration(): Long {
    return authorizationCodeValidDuration
  }

  fun tokenSignedKeyId(): String {
    return tokenSignedKeyId
  }

  fun idTokenSignedKeyId(): String {
    return idTokenSignedKeyId
  }

  fun accessTokenDuration(): Long {
    return accessTokenDuration
  }

  fun refreshTokenDuration(): Long {
    return refreshTokenDuration
  }

  fun idTokenDuration(): Long {
    return idTokenDuration
  }

  fun idTokenStrictMode(): Boolean {
    return idTokenStrictMode
  }

  fun hasTokenEndpoint(): Boolean {
    return Objects.nonNull(tokenEndpoint) && !tokenEndpoint.isEmpty()
  }

  fun hasUserinfoEndpoint(): Boolean {
    return Objects.nonNull(userinfoEndpoint) && !userinfoEndpoint.isEmpty()
  }

  fun hasRegistrationEndpoint(): Boolean {
    return Objects.nonNull(revocationEndpoint) && !registrationEndpoint.isEmpty()
  }

  fun isSupportedResponseType(responseType: ResponseType): Boolean {
    return responseTypesSupported.contains(responseType.value)
  }

  fun hasScopesSupported(): Boolean {
    return !scopesSupported.isEmpty()
  }

  fun hasResponseTypesSupported(): Boolean {
    return !responseTypesSupported.isEmpty()
  }

  fun hasResponseModesSupported(): Boolean {
    return !responseModesSupported.isEmpty()
  }

  fun hasGrantTypesSupported(): Boolean {
    return !grantTypesSupported.isEmpty()
  }

  fun hasAcrValuesSupported(): Boolean {
    return !acrValuesSupported.isEmpty()
  }

  fun hasSubjectTypesSupported(): Boolean {
    return !subjectTypesSupported.isEmpty()
  }

  fun hasIdTokenSigningAlgValuesSupported(): Boolean {
    return !idTokenSigningAlgValuesSupported.isEmpty()
  }

  fun hasIdTokenEncryptionAlgValuesSupported(): Boolean {
    return !idTokenEncryptionAlgValuesSupported.isEmpty()
  }

  fun hasIdTokenEncryptionEncValuesSupported(): Boolean {
    return !idTokenEncryptionEncValuesSupported.isEmpty()
  }

  fun hasUserinfoSigningAlgValuesSupported(): Boolean {
    return !userinfoSigningAlgValuesSupported.isEmpty()
  }

  fun hasUserinfoEncryptionAlgValuesSupported(): Boolean {
    return !userinfoEncryptionAlgValuesSupported.isEmpty()
  }

  fun hasUserinfoEncryptionEncValuesSupported(): Boolean {
    return !userinfoEncryptionEncValuesSupported.isEmpty()
  }

  fun hasRequestObjectSigningAlgValuesSupported(): Boolean {
    return !requestObjectSigningAlgValuesSupported.isEmpty()
  }

  fun hasRequestObjectEncryptionAlgValuesSupported(): Boolean {
    return !requestObjectEncryptionAlgValuesSupported.isEmpty()
  }

  fun hasRequestObjectEncryptionEncValuesSupported(): Boolean {
    return !requestObjectEncryptionEncValuesSupported.isEmpty()
  }

  fun hasAuthorizationSigningAlgValuesSupported(): Boolean {
    return !authorizationSigningAlgValuesSupported.isEmpty()
  }

  fun hasAuthorizationEncryptionAlgValuesSupported(): Boolean {
    return !authorizationEncryptionAlgValuesSupported.isEmpty()
  }

  fun hasAuthorizationEncryptionEncValuesSupported(): Boolean {
    return !authorizationEncryptionEncValuesSupported.isEmpty()
  }

  fun hasTokenEndpointAuthMethodsSupported(): Boolean {
    return !tokenEndpointAuthMethodsSupported.isEmpty()
  }

  fun hasTokenEndpointAuthSigningAlgValuesSupported(): Boolean {
    return !tokenEndpointAuthSigningAlgValuesSupported.isEmpty()
  }

  fun hasDisplayValuesSupported(): Boolean {
    return !displayValuesSupported.isEmpty()
  }

  fun hasClaimTypesSupported(): Boolean {
    return !claimTypesSupported.isEmpty()
  }

  fun hasClaimsSupported(): Boolean {
    return !claimsSupported.isEmpty()
  }

  fun hasRevocationEndpoint(): Boolean {
    return Objects.nonNull(revocationEndpoint) && !revocationEndpoint.isEmpty()
  }

  fun hasRevocationEndpointAuthMethodsSupported(): Boolean {
    return !revocationEndpointAuthMethodsSupported.isEmpty()
  }

  fun hasRevocationEndpointAuthSigningAlgValuesSupported(): Boolean {
    return !revocationEndpointAuthSigningAlgValuesSupported.isEmpty()
  }

  fun hasIntrospectionEndpoint(): Boolean {
    return Objects.nonNull(introspectionEndpoint) && !introspectionEndpoint.isEmpty()
  }

  fun hasIntrospectionEndpointAuthMethodsSupported(): Boolean {
    return !introspectionEndpointAuthMethodsSupported.isEmpty()
  }

  fun hasIntrospectionEndpointAuthSigningAlgValuesSupported(): Boolean {
    return !introspectionEndpointAuthSigningAlgValuesSupported.isEmpty()
  }

  fun hasCodeChallengeMethodsSupported(): Boolean {
    return !codeChallengeMethodsSupported.isEmpty()
  }

  fun backchannelTokenDeliveryModesSupported(): List<String>? {
    return backchannelTokenDeliveryModesSupported
  }

  fun hasBackchannelTokenDeliveryModesSupported(): Boolean {
    return !backchannelTokenDeliveryModesSupported.isEmpty()
  }

  fun backchannelAuthenticationEndpoint(): String? {
    return backchannelAuthenticationEndpoint
  }

  fun hasBackchannelAuthenticationEndpoint(): Boolean {
    return (Objects.nonNull(backchannelAuthenticationEndpoint) &&
        !backchannelAuthenticationEndpoint.isEmpty())
  }

  fun backchannelAuthenticationRequestSigningAlgValuesSupported(): List<String>? {
    return backchannelAuthenticationRequestSigningAlgValuesSupported
  }

  fun hasBackchannelAuthenticationRequestSigningAlgValuesSupported(): Boolean {
    return !backchannelAuthenticationRequestSigningAlgValuesSupported.isEmpty()
  }

  fun backchannelUserCodeParameterSupported(): Boolean {
    return backchannelUserCodeParameterSupported!!
  }

  fun hasBackchannelUserCodeParameterSupported(): Boolean {
    return Objects.nonNull(backchannelUserCodeParameterSupported)
  }

  fun isSupportedGrantType(grantType: String): Boolean {
    return grantTypesSupported.contains(grantType)
  }

  fun defaultMaxAge(): Long {
    return defaultMaxAge
  }

  fun authorizationDetailsTypesSupported(): List<String>? {
    return authorizationDetailsTypesSupported
  }

  fun isSupportedAuthorizationDetailsType(type: String): Boolean {
    return authorizationDetailsTypesSupported.contains(type)
  }

  fun authorizationResponseDuration(): Long {
    return authorizationResponseDuration
  }

  fun hasKey(algorithm: String?): Boolean {
    return jwks!!.contains(algorithm!!)
  }
}
