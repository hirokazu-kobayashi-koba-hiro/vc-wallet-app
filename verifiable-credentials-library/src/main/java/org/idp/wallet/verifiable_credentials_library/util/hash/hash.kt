package org.idp.wallet.verifiable_credentials_library.util.hash

import java.security.MessageDigest
import java.util.Base64

fun calculateHashWithSha256(data: String): String {
  val digest = MessageDigest.getInstance("SHA-256")
  val hash = digest.digest(data.toByteArray())
  return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
}
