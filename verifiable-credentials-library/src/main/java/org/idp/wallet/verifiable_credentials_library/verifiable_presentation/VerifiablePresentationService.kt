package org.idp.wallet.verifiable_credentials_library.verifiable_presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import org.idp.wallet.verifiable_credentials_library.basic.http.HttpClient
import org.idp.wallet.verifiable_credentials_library.basic.jose.JoseHandler
import org.idp.wallet.verifiable_credentials_library.basic.jose.JwtObject
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import java.net.URLDecoder


class VerifiablePresentationService(context: Context) {

    val registry = VerifiableCredentialRegistry(context)

    suspend fun handleVpRequest(url: String): VerifiableCredentialsRecords {
        Log.d("Vc library", "handleVpRequest")
        //extract RequestObject
        val requestObject = extractRequestObject(url)
        val verifiablePresentationRequestCreator = VerifiablePresentationRequestCreator(requestObject)
        val presentationRequest = verifiablePresentationRequestCreator.create()
        //verify request

        //find vc
        val records = registry.getAllAsCollection()
        val filterVerifiableCredential =
            presentationRequest.filterVerifiableCredential(records)
        //create id_token and vp_token and presentation_submission
        //create response
        //return response

        return filterVerifiableCredential
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