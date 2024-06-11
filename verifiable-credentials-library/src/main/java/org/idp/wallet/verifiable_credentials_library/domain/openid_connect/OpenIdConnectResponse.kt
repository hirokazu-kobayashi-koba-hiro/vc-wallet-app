package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse

class OpenIdConnectResponse(
    val tokenResponse: TokenResponse,
    val userinfoResponse: UserinfoResponse?
) {}
