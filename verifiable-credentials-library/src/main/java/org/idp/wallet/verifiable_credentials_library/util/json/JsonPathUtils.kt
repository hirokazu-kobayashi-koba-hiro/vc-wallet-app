package org.idp.wallet.verifiable_credentials_library.util.json

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read

object JsonPathUtils {

  fun readAsString(json: String, path: String): String? {
    return JsonPath.parse(json)?.read<String>(path)
  }

  fun readAsListString(json: String, path: String): List<String>? {
    return JsonPath.parse(json)?.read<List<String>>(path)
  }
}
