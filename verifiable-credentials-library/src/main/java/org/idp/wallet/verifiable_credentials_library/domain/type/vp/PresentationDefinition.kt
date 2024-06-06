package org.idp.wallet.verifiable_credentials_library.domain.type.vp

data class PresentationDefinition(
    val id: String = "",
    val inputDescriptors: List<InputDescriptorDetail> = listOf()
) {}

data class InputDescriptorDetail(
    val id: String,
    val name: String?,
    val purpose: String?,
    val format: Format?,
    val constraints: Constraints
) {}

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
