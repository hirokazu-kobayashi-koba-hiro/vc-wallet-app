package org.idp.wallet.verifiable_credentials_library.basic.http

import android.net.Uri
import java.net.URLDecoder

fun extractQueries(url: String): Map<String, List<String>> {
  val uri = Uri.parse(url)
  val query = uri.query
  val splittedQueries = query?.split("&")
  val mutableMapOf = mutableMapOf<String, MutableList<String>>()
  splittedQueries?.let { queryString ->
    queryString.forEach {
      val queryValue = it.split("=")
      val key = URLDecoder.decode(queryValue[0])
      val value = URLDecoder.decode(queryValue[1])
      val strings = mutableMapOf[key]
      strings?.let {
        it.add(key)
        mutableMapOf.put(key, it)
      } ?: mutableMapOf.put(key, mutableListOf(value))
    }
  }
  return mutableMapOf
}
