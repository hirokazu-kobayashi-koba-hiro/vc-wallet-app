package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import id.walt.sdjwt.SDJwt
import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.basic.jose.JwtObject
import org.json.JSONObject
import java.net.URLDecoder
import java.util.ArrayList
import kotlin.js.ExperimentalJsExport


class VerifiablePresentationService(context: Context) {

    val registry = VerifiableCredentialRegistry(context)

    suspend fun handleVpRequest(url: String) {
        Log.d("Vc library", "handleVpRequest")
        //extract RequestObject
        val requestObject = extractRequestObject(url)
        val verifiablePresentationRequestCreator = VerifiablePresentationRequestCreator(requestObject)
        val presentationRequest = verifiablePresentationRequestCreator.create()
        presentationRequest.presentationDefinition
        //verify request
        //find vc
        //create id_token and vp_token and presentation_submission
        //create response
        //return response
    }

    @OptIn(ExperimentalJsExport::class)
    fun decideVerifiableCredential(presentationDefinition: PresentationDefinition): List<JSONObject> {
        val allCredentials = registry.getAll()
        val vcList = mutableListOf<JSONObject>()
        val filteredVcList = mutableListOf<JSONObject>()
        allCredentials.keys().forEach { key ->
            val jsonArray = allCredentials.getJSONArray(key)
            for (i in 0 until jsonArray.length()) {
                val value = jsonArray.getString(i)
                val sdJwt = SDJwt.parse(value)
                val fullPayload = sdJwt.fullPayload
                val jsonObject = JSONObject(fullPayload)
                vcList.add(jsonObject)
            }
        }
        val inputDescriptors = presentationDefinition.inputDescriptors
        inputDescriptors.forEach { inputDescriptorDetail ->
            inputDescriptorDetail.constraints?.fields?.forEach { field ->
                val path = field.path[0]
                vcList.forEach {vcJsonObject ->
                    if (path.contains("type")) {
                        val typeList = JsonPath.parse(vcJsonObject.toString())?.read<List<String>>(path)
                        if (typeList != null && field.pattern != null) {
                            if (typeList.contains(field.pattern)) {
                                filteredVcList.add(vcJsonObject)
                            }
                        }
                    }
                }
            }
        }
        return filteredVcList
    }



    private suspend fun extractRequestObject(url: String): JwtObject {
        val uri = Uri.parse(url)
        val encodedRequestUri = uri.getQueryParameter("request_uri")
        if (encodedRequestUri != null) {
            val decodedCredentialOfferUri = URLDecoder.decode(encodedRequestUri)
            val requestObjectResponse = HttpClient.get(decodedCredentialOfferUri)
            return JoseHandler.parse(requestObjectResponse.toString())
        }
        val requestObject = uri.getQueryParameter("request")
        if (requestObject != null) {
            return JoseHandler.parse(requestObject)
        }
        throw RuntimeException("Authorization request does not contain request object. Authorization request must contain either request or request_uri.")
    }
}