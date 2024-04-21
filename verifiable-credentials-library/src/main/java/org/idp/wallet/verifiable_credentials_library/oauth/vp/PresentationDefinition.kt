package org.idp.wallet.verifiable_credentials_library.oauth.vp

import org.idp.wallet.verifiable_credentials_library.basic.json.JsonPathUtils
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords

class PresentationDefinition(
    val id: String = "",
    private val inputDescriptors: List<InputDescriptorDetail> = listOf()
) {

  fun filterVerifiableCredential(
      verifiableCredentialsRecords: VerifiableCredentialsRecords
  ): VerifiableCredentialsRecords {
    val filteredVcList = mutableSetOf<VerifiableCredentialsRecord>()
    inputDescriptors.forEach { inputDescriptorDetail ->
      inputDescriptorDetail.constraints?.fields?.forEach { field ->
        field.path.forEach { path ->
          verifiableCredentialsRecords.forEach { vcRecord ->
            if (path.contains("type")) {
              val typeList =
                  JsonPathUtils.readAsListString(vcRecord.getPayloadWithJson().toString(), path)
              if (typeList != null && field.filter?.pattern != null) {
                if (typeList.contains(field.filter.pattern)) {
                  filteredVcList.add(vcRecord)
                }
              }
            }
          }
        }
      }
    }
    return VerifiableCredentialsRecords(filteredVcList.toList())
  }
}

data class InputDescriptorDetail(
    val id: String,
    val name: String?,
    val purpose: String?,
    val format: Format?,
    val constraints: Constraints?
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
)

data class Filter(val type: String, val pattern: String)
