package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import org.json.JSONObject

class VerifiableCredentialsRecords(private val values: List<VerifiableCredentialsRecord>) :
    Iterable<VerifiableCredentialsRecord> {
  constructor() : this(ArrayList())

  fun add(record: VerifiableCredentialsRecord): VerifiableCredentialsRecords {
    val arrayList = ArrayList(values)
    arrayList.add(record)
    return VerifiableCredentialsRecords(arrayList)
  }

  fun find(ids: List<String>): VerifiableCredentialsRecords {
    val filtered = values.filter { ids.contains(it.id) }
    return VerifiableCredentialsRecords(filtered)
  }

  fun find(id: String): VerifiableCredentialsRecord? {
    return values.find { it.id == id }
  }

  fun rawVcList(): List<String> {
    return values.map { it.rawVc }
  }

  override fun iterator(): Iterator<VerifiableCredentialsRecord> {
    return values.iterator()
  }

  fun size(): Int {
    return values.size
  }
}

class VerifiableCredentialsRecord(
    val id: String,
    val format: String,
    val rawVc: String,
    val payload: Map<String, Any>
) {

  fun getPayloadWithJson(): JSONObject {
    return JSONObject(payload)
  }

  fun isSdJwt(): Boolean {
    return format == "vc+sd-jwt"
  }

  fun isJwt(): Boolean {
    return format == "jwt_vc_json"
  }

  fun isLdp(): Boolean {
    return format == "ldp_vc"
  }
}
