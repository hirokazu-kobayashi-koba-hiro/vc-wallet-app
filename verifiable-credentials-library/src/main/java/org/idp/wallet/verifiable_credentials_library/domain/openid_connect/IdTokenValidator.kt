package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import org.idp.wallet.verifiable_credentials_library.domain.error.OidcError
import org.idp.wallet.verifiable_credentials_library.domain.error.OpenIdConnectException
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.JwksResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils

class IdTokenValidator(
    private val request: OpenIdConnectRequest,
    private val tokenResponse: TokenResponse,
    private val jwkResponse: JwksResponse
) {

  fun validate() {
    if (tokenResponse.idToken == null) {
      throw OpenIdConnectException(OidcError.NOT_FOUND_REQUIRED_PARAMS, "not found id_token")
    }
    val parsedIdToken = JoseUtils.parseAndVerifySignature(tokenResponse.idToken, jwkResponse.jwks())
    parsedIdToken.kid()
  }
}
