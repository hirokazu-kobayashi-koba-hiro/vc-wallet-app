package org.idp.wallet.verifiable_credentials_library.basic.extension

import org.json.JSONArray

fun JSONArray.toListAsString(): List<String> {
  val list = mutableListOf<String>()
  for (i in 0 until this.length()) {
    list.add(this[i] as String)
  }
  return list
}
