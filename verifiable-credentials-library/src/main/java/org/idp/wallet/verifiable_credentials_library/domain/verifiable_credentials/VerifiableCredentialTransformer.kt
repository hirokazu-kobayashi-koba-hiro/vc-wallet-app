package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import java.util.UUID
import org.idp.wallet.verifiable_credentials_library.domain.error.VcError
import org.idp.wallet.verifiable_credentials_library.domain.error.VerifiableCredentialsException
import org.idp.wallet.verifiable_credentials_library.domain.type.vc.VerifiableCredentialsType
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.sdjwt.SdJwtUtils

class VerifiableCredentialTransformer(
    private val issuer: String,
    private val verifiableCredentialsType: VerifiableCredentialsType,
    private val type: String,
    private val rawVc: String,
    private val jwks: String
) {

  suspend fun transform(): VerifiableCredentialsRecord {
    val uuid = UUID.randomUUID().toString()
    val format = verifiableCredentialsType.format
    return when (verifiableCredentialsType) {
      VerifiableCredentialsType.SD_JWT -> {
        val claims = SdJwtUtils.parseAndVerifySignature(rawVc, jwks)
        VerifiableCredentialsRecord(uuid, issuer, type, format, rawVc, claims)
      }
      VerifiableCredentialsType.JWT_VC_JSON -> {
        val jwt = JoseUtils.parseAndVerifySignature(rawVc, jwks)
        val payload = jwt.payload()
        VerifiableCredentialsRecord(uuid, issuer, type, format, rawVc, payload)
      }
      VerifiableCredentialsType.MSO_MDOC -> {
        VerifiableCredentialsRecord(uuid, issuer, type, format, rawVc, mapOf())
      }
      else -> {
        throw VerifiableCredentialsException(
            VcError.UNSUPPORTED_CREDENTIAL_FORMAT, "unsupported format")
      }
    }
  }
}
