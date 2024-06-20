package org.idp.wallet.verifiable_credentials_library.util.json

import com.github.jsonldjava.utils.JsonUtils
import com.nimbusds.jose.shaded.gson.FieldNamingPolicy
import com.nimbusds.jose.shaded.gson.Gson
import com.nimbusds.jose.shaded.gson.GsonBuilder

object JsonUtils {

  private val gson = Gson()
  private val gsonWithSnakeCaseStrategy =
      GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

  fun write(value: Any): String {
    return gson.toJson(value)
  }

  fun <T> read(value: String, type: Class<T>, snakeCase: Boolean = true): T {
    if (snakeCase) {
      return gsonWithSnakeCaseStrategy.fromJson(value, type)
    }
    return gson.fromJson(value, type)
  }

  fun toPrettyString(data: Any): String {
    return JsonUtils.toPrettyString(data)
  }
}
