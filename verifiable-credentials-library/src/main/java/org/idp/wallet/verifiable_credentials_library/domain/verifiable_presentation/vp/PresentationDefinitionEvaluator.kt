package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.vp

import org.idp.wallet.verifiable_credentials_library.util.json.JsonPathUtils
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.Constraints
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.Field
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.InputDescriptorDetail
import org.idp.wallet.verifiable_credentials_library.domain.type.vp.PresentationDefinition
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.json.JSONObject

class PresentationDefinitionEvaluator(
  private val presentationDefinition: PresentationDefinition,
  private val verifiableCredentialsRecords: VerifiableCredentialsRecords
) {

  fun evaluate(): PresentationDefinitionEvaluation {
    val definitionId = presentationDefinition.id
    val inputDescriptors = presentationDefinition.inputDescriptors
    val result = mutableMapOf<InputDescriptorDetail, VerifiableCredentialsRecords>()
    inputDescriptors.forEach { inputDescriptor ->
      verifiableCredentialsRecords.forEach { record ->
        if (match(inputDescriptor.constraints, record.getPayloadWithJson())) {
          val registered = result[inputDescriptor]
          registered?.let {
            it.add(record)
            result.put(inputDescriptor, it)
          } ?: result.put(inputDescriptor, VerifiableCredentialsRecords(mutableListOf(record)))
        }
      }
    }
    return PresentationDefinitionEvaluation(definitionId = definitionId, results = result)
  }

  private fun match(constraints: Constraints, jsonObject: JSONObject): Boolean {
    constraints.fields?.forEach { field ->
      if (field.path.stream().noneMatch { path -> match(path, field, jsonObject) }) {
        return false
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
