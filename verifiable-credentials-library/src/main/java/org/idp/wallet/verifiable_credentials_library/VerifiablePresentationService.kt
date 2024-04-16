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

    suspend fun handleVpRequest(url: String):VerifiableCredentialsRecords {
        Log.d("Vc library", "handleVpRequest")
        //extract RequestObject
        val requestObject = extractRequestObject(url)
        val verifiablePresentationRequestCreator = VerifiablePresentationRequestCreator(requestObject)
        val presentationRequest = verifiablePresentationRequestCreator.create()
        val records = registry.getAllAsCollection()
        return filterVerifiableCredential(records ,presentationRequest.presentationDefinition)
        //verify request
        //find vc
        //create id_token and vp_token and presentation_submission
        //create response
        //return response
    }


    fun filterVerifiableCredential(verifiableCredentialsRecords: VerifiableCredentialsRecords, presentationDefinition: PresentationDefinition?): VerifiableCredentialsRecords {
        val filteredVcList = mutableListOf<VerifiableCredentialsRecord>()
        val inputDescriptors = presentationDefinition?.inputDescriptors
        inputDescriptors?.forEach { inputDescriptorDetail ->
            inputDescriptorDetail.constraints?.fields?.forEach { field ->
                val path = field.path[0]
                verifiableCredentialsRecords.forEach { vcRecord ->
                    if (path.contains("type")) {
                        val typeList = JsonPath.parse(vcRecord.getPayloadWithJson().toString())?.read<List<String>>(path)
                        if (typeList != null && field.filter?.pattern != null) {
                            if (typeList.contains(field.filter.pattern)) {
                                filteredVcList.add(vcRecord)
                            }
                        }
                    }
                }
            }
        }
        return VerifiableCredentialsRecords(filteredVcList)
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