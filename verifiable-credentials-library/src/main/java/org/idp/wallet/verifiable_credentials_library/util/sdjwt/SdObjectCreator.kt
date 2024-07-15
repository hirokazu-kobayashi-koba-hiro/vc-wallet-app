package org.idp.wallet.verifiable_credentials_library.util.sdjwt

import eu.europa.ec.eudi.sdjwt.SdObject
import eu.europa.ec.eudi.sdjwt.plain
import eu.europa.ec.eudi.sdjwt.sd
import eu.europa.ec.eudi.sdjwt.sdJwt
import eu.europa.ec.eudi.sdjwt.structured
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

object SdObjectCreator {

  fun create(sdJwtPayload: SdJwtPayload): SdObject {
    return sdJwt {
      sdJwtPayload.plainPayload.forEach { element ->
        when (element.value) {
          is String ->
              if (element.disclosable) plain { put(element.key, element.value) }
              else sd { put(element.key, element.value) }
          is List<*> ->
              if (element.disclosable) {
                plain {
                  putJsonArray(element.key) {
                    element.value.forEach { v -> if (v is String) add(v) }
                  }
                }
              } else {
                sd {
                  putJsonArray(element.key) {
                    element.value.forEach { v -> if (v is String) add(v) }
                  }
                }
              }
          is Map<*, *> ->
              if (element.disclosable) {
                plain {
                  putJsonObject(element.key) {
                    element.value.forEach { (k, v) -> if (k is String && v is String) put(k, v) }
                  }
                }
              } else {
                sd {
                  putJsonObject(element.key) {
                    element.value.forEach { (k, v) -> if (k is String && v is String) put(k, v) }
                  }
                }
              }
        }
      }

      sdJwtPayload.structuredPayload.forEach { (key, elements) ->
        structured(key) {
          elements.forEach { element ->
            when (element.value) {
              is String ->
                  if (element.disclosable) plain { put(element.key, element.value) }
                  else sd { put(element.key, element.value) }
              is List<*> ->
                  if (element.disclosable) {
                    plain {
                      putJsonArray(element.key) {
                        element.value.forEach { v -> if (v is String) add(v) }
                      }
                    }
                  } else {
                    sd {
                      putJsonArray(element.key) {
                        element.value.forEach { v -> if (v is String) add(v) }
                      }
                    }
                  }
              is Map<*, *> ->
                  if (element.disclosable) {
                    plain {
                      putJsonObject(element.key) {
                        element.value.forEach { (k, v) ->
                          if (k is String && v is String) put(k, v)
                        }
                      }
                    }
                  } else {
                    sd {
                      putJsonObject(element.key) {
                        element.value.forEach { (k, v) ->
                          if (k is String && v is String) put(k, v)
                        }
                      }
                    }
                  }
            }
          }
        }
      }
    }
  }
}
