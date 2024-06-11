package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import org.idp.wallet.verifiable_credentials_library.domain.error.OidcError
import org.idp.wallet.verifiable_credentials_library.domain.error.OpenIdConnectException
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.AuthenticationResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest

class AuthenticationResponseValidator(
    private val openIdConnectRequest: OpenIdConnectRequest,
    private val authenticationResponse: AuthenticationResponse
) {

  fun validate() {
    authenticationResponse.error()?.let {
      throw OpenIdConnectException(
          OidcError.NOT_AUTHENTICATED, authenticationResponse.errorDescription())
    }
    if (openIdConnectRequest.isResponseTypeCode() && authenticationResponse.code().isBlank()) {
      throw OpenIdConnectException(OidcError.NOT_FOUND_REQUIRED_PARAMS)
    }
  }
}
