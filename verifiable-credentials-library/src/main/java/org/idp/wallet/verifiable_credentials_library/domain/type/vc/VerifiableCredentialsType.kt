package org.idp.wallet.verifiable_credentials_library.domain.type.vc

import org.idp.wallet.verifiable_credentials_library.domain.error.VcError
import org.idp.wallet.verifiable_credentials_library.domain.error.VerifiableCredentialsException

enum class VerifiableCredentialsType(val format: String, val doctype: String) {
  MSO_MDOC("mso_mdoc", "org.iso.18013.5.1.mDL"),
  SD_JWT("vc+sd-jwt", ""),
  JWT_VC_JSON("jwt_vc_json", ""),
  DID_JWT_VC("did_jwt_vc", ""),
  JWT_VC_JSON_LD("jwt_vc_json-ld", ""),
  LDP_VC("ldp_vc", "");

  companion object {
    fun of(format: String): VerifiableCredentialsType {
      for (type: VerifiableCredentialsType in entries) {
        if (type.format == format) {
          return type
        }
      }
      throw VerifiableCredentialsException(
          VcError.UNSUPPORTED_CREDENTIAL_FORMAT, String.format("not found format (%s)", format))
    }
  }
}
