package org.idp.wallet.verifiable_credentials_library.util.json

import com.github.jsonldjava.core.JsonLdOptions
import com.github.jsonldjava.core.JsonLdProcessor

object JsonLdUtils {

  fun normalize(value: Map<*, *>): String {
    val normalized = JsonLdProcessor.normalize(value, JsonLdOptions("json-ld-1.1"))
    return JsonUtils.toPrettyString(normalized)
  }
}
