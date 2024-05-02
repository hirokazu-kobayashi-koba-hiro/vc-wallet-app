package org.idp.wallet.verifiable_credentials_library.type.oauth

import java.util.Arrays
import java.util.stream.Collectors

/**
 * ResponseType
 *
 * <p>All but the code Response Type value, which is defined by OAuth 2.0 [RFC6749], are defined in
 * the OAuth 2.0 Multiple Response Type Encoding Practices [OAuth.Responses] specification. NOTE:
 * While OAuth 2.0 also defines the token Response Type value for the Implicit Flow, OpenID Connect
 * does not use this Response Type, since no ID Token would be returned.
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#Authentication">3.
 *   Authentication</a>
 */
enum class ResponseType(private val values: Set<String>, val value: String) {
  code(setOf("code"), "code"),
  token(setOf("token"), "token"),
  id_token(setOf("id_token"), "id_token"),
  code_token(setOf("code", "token"), "code token"),
  code_token_id_token(setOf("code", "token", "id_token"), "code token id_token"),
  code_id_token(setOf("code", "id_token"), "code id_token"),
  token_id_token(setOf("token", "id_token"), "token id_token"),
  none(setOf("none"), "none"),
  vp_token(setOf("vp_token"), "vp_token"),
  vp_token_id_token(setOf("vp_token", "id_token"), "vp_token id_token"),
  undefined(setOf(), ""),
  unknown(setOf(), "");

  companion object {
    fun of(input: String?): ResponseType {
      if (input == null || input.isEmpty()) {
        return undefined
      }
      val inputValues =
          Arrays.stream(input.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
              .collect(Collectors.toSet())
      for (responseType in entries) {
        if (responseType.values.size == inputValues.size &&
            responseType.values.containsAll(inputValues)) {
          return responseType
        }
      }
      return unknown
    }
  }

  fun isAuthorizationCodeFlow(): Boolean {
    return this === code
  }

  fun isOAuthImplicitFlow(): Boolean {
    return this === token
  }

  fun isOidcImplicitFlow(): Boolean {
    return this === id_token || this === token_id_token
  }

  fun isHybridFlow(): Boolean {
    return this === code_token || this === code_id_token || this === code_token_id_token
  }

  fun isOidcHybridFlow(): Boolean {
    return this === code_id_token || this === code_token_id_token
  }

  fun isUndefined(): Boolean {
    return this === undefined
  }

  fun isUnknown(): Boolean {
    return this === unknown
  }

  fun isCodeIdToken(): Boolean {
    return this === code_id_token
  }

  fun isCode(): Boolean {
    return this === code
  }
}
