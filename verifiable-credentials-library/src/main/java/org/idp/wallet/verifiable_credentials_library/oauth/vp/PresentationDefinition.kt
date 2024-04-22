package org.idp.wallet.verifiable_credentials_library.oauth.vp

import org.idp.wallet.verifiable_credentials_library.basic.json.JsonPathUtils
import org.json.JSONObject

class PresentationDefinition(
    val id: String = "",
    private val inputDescriptors: List<InputDescriptorDetail> = listOf()
) {

  fun evaluate(jsonObject: JSONObject): Boolean {
    inputDescriptors.forEach { inputDescriptorDetail ->
      inputDescriptorDetail.constraints.fields?.forEach { field ->
        if (field.path.stream().noneMatch { path -> match(path, field, jsonObject) }) {
          return false
        }
      }
    }
    return true
  }

  private fun match(path: String, field: Field, jsonObject: JSONObject): Boolean {
    if (path.contains("type")) {
      val typeList = JsonPathUtils.readAsListString(jsonObject.toString(), path)
      if (typeList == null && field.isRequired()) {
        return false
      }
      if (typeList != null &&
          field.filter?.pattern != null &&
          !typeList.contains(field.filter.pattern)) {
        return false
      }
    } else {
      val value = JsonPathUtils.readAsString(jsonObject.toString(), path)
      if (value == null && field.isRequired()) {
        return false
      }
      if (value != null && field.filter?.pattern != null && value != field.filter.pattern) {
        return false
      }
    }
    return true
  }
}

data class InputDescriptorDetail(
    val id: String,
    val name: String?,
    val purpose: String?,
    val format: Format?,
    val constraints: Constraints
)

data class Format(
    val jwt: FormatDetail?,
    val jwtVc: FormatDetail?,
    val jwtVp: FormatDetail?,
    val ldpVc: FormatDetail?,
    val ldpVp: FormatDetail?,
    val ldp: FormatDetail?
)

data class FormatDetail(val alg: List<String>?, val proofType: List<String>?)

data class Constraints(val limitDisclosure: String?, val fields: List<Field>?)

data class Field(
    val path: List<String>,
    val id: String?,
    val purpose: String?,
    val name: String?,
    val filter: Filter?,
    val optional: Boolean?,
    val pattern: String?
) {
  fun isRequired(): Boolean {
    return optional == false || optional == null
  }
}

data class Filter(val type: String, val pattern: String)
