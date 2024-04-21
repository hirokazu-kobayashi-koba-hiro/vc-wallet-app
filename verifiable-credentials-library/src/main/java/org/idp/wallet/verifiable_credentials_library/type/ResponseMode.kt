package org.idp.wallet.verifiable_credentials_library.type

enum class ResponseMode(val value: String, val responseModeValue: String) {
  query("query", "?"),
  fragment("fragment", "#"),
  form_post("form_post", ""),
  query_jwt("query.jwt", "?"),
  fragment_jwt("fragment.jwt", "#"),
  form_post_jwt("form_post.jwt", ""),
  jwt("jwt", ""),
  direct_post("direct_post", ""),
  undefined("", ""),
  unknown("", "");

  companion object {
    fun of(value: String?): ResponseMode {
      if (value.isNullOrEmpty()) {
        return undefined
      }
      for (responseMode in entries) {
        if (responseMode.value == value) {
          return responseMode
        }
      }
      return unknown
    }
  }

  fun responseModeValue(): String? {
    return responseModeValue
  }

  fun isDefinedResponseModeValue(): Boolean {
    return !responseModeValue.isEmpty()
  }

  fun isDefined(): Boolean {
    return this !== undefined
  }

  fun isJwtMode(): Boolean {
    return this === query_jwt || this === fragment_jwt || this === jwt || this === form_post_jwt
  }

  fun isJwt(): Boolean {
    return this === jwt
  }
}
