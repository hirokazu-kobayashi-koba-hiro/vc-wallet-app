package org.idp.wallet.verifiable_credentials_library.type.vp

enum class ClientIdScheme(val value: String) {
  pre_registered("pre-registered"),
  redirect_uri("redirect_uri"),
  unknown("unknown"),
  undefined("undefined");

  companion object {
    fun of(value: String?): ClientIdScheme {
      if (value.isNullOrEmpty()) {
        return undefined
      }
      for (client in entries) {
        if (client.value == value) {
          return client
        }
      }
      return undefined
    }
  }
}
