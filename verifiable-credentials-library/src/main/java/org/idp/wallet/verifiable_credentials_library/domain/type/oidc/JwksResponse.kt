package org.idp.wallet.verifiable_credentials_library.domain.type.oidc

import org.json.JSONObject

data class JwksResponse(val content: JSONObject) {
  fun jwks(): String {
    return content.toString()
  }
}
