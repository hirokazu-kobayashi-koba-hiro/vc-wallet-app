package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
class VerifiableCredentialRegistry(context: Context) {

    private val sharedRef = context.getSharedPreferences("idp_verifiable_credentials_library", Context.MODE_PRIVATE)

    fun save(credentialIssuer: String, vc: String) {
        val vcValues = getAll()
        val optJSONArray = vcValues.optJSONArray(credentialIssuer)
        val jsonArray = optJSONArray?: JSONArray()
        jsonArray.put(vc)
        vcValues.put(credentialIssuer, jsonArray)
        sharedRef.edit().putString("vc", vcValues.toString()).apply()
    }

    fun getAll(): JSONObject {
        val vcRef = sharedRef.getString("vc", null)
        return JSONObject(vcRef ?: "{}")
    }

    fun find(credentialIssuer: String): JSONArray? {
        val vcValues = getAll()
        return vcValues.optJSONArray(credentialIssuer)
    }
}