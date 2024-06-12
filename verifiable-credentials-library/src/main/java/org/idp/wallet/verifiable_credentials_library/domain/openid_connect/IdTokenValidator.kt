package org.idp.wallet.verifiable_credentials_library.domain.openid_connect

import java.time.LocalDateTime
import java.time.ZoneOffset
import org.idp.wallet.verifiable_credentials_library.domain.error.OidcError
import org.idp.wallet.verifiable_credentials_library.domain.error.OpenIdConnectException
import org.idp.wallet.verifiable_credentials_library.domain.type.oauth.TokenResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.JwksResponse
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.jose.JwtObject

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
    validateIssuer(parsedIdToken)
    validateAud(parsedIdToken)
    //    validateExp(parsedIdToken)
    validateNonce(parsedIdToken)
  }

  private fun validateIssuer(parsedIdToken: JwtObject) {
    if (!parsedIdToken.containsKey("iss")) {
      throw OpenIdConnectException(
          OidcError.NOT_FOUND_REQUIRED_PARAMS, "iss must contain in idToken")
    }
    if (parsedIdToken.valueAsStringFromPayload("iss") != request.issuer &&
        parsedIdToken.valueAsStringFromPayload("iss") != request.issuer + "/") {
      throw OpenIdConnectException(OidcError.INVALID_ID_TOKEN, "iss must be same value of issuer")
    }
  }

  private fun validateAud(parsedIdToken: JwtObject) {
    if (!parsedIdToken.containsKey("aud")) {
      throw OpenIdConnectException(
          OidcError.NOT_FOUND_REQUIRED_PARAMS, "aud must contains in idToken")
    }
    parsedIdToken.valueAsStringListFromPayload("aud")?.let {
      if (!it.contains(request.clientId)) {
        throw OpenIdConnectException(
            OidcError.INVALID_ID_TOKEN, "aud must contain same value of clientId")
      }
    }
  }

  private fun validateExp(parsedIdToken: JwtObject) {
    parsedIdToken.valueAsLongFromPayload("exp")?.let {
      if (it < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
        throw OpenIdConnectException(
            OidcError.INVALID_ID_TOKEN, "aud must contain same value of exp")
      }
    }
        ?: throw OpenIdConnectException(
            OidcError.NOT_FOUND_REQUIRED_PARAMS, "exp must contains in idToken")
  }

  private fun validateNonce(parsedIdToken: JwtObject) {
    request.nonce?.let { requestedNonce ->
      parsedIdToken.valueAsStringFromPayload("nonce")?.let {
        if (it != requestedNonce) {
          throw OpenIdConnectException(
              OidcError.INVALID_ID_TOKEN,
              "nonce must be same value of request, if request include nonce.")
        }
      }
          ?: throw OpenIdConnectException(
              OidcError.INVALID_ID_TOKEN,
              "nonce must contain in idToken, if request include nonce.")
    }
  }
}
