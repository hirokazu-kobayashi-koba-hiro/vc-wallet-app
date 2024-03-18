package org.idp.wallet.app

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.json.JSONObject

class VerifiableCredentialIssuanceViewModel :
    ViewModel() {

    var _vcContent = MutableStateFlow(JSONObject())
    val vciState = _vcContent.asStateFlow()
    suspend fun request(context: Context, uri: String, format: String) {
        var verifiableCredentialsClient = VerifiableCredentialsClient(context, "218232426")
        var requestVCIResponse = verifiableCredentialsClient.requestVCI(uri, format)
        Log.d("Vc app", requestVCIResponse.toString())
    }

    fun getAllCredentials(context: Context) {
        var verifiableCredentialsClient = VerifiableCredentialsClient(context, "218232426")
        var allCredentials = verifiableCredentialsClient.getAllCredentials()
        Log.d("Vc app", allCredentials.toString())
        _vcContent.value = allCredentials
    }

}