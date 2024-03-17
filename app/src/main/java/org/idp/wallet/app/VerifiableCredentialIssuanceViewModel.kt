package org.idp.wallet.app

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.json.JSONObject

class VerifiableCredentialIssuanceViewModel :
    ViewModel() {

    var _vcContent = MutableStateFlow("")
    val vciState = _vcContent.asStateFlow()
    suspend fun request(context: Context, uri: String, format: String) {
        var verifiableCredentialsClient = VerifiableCredentialsClient(context, "218232426")
        var requestVCIResponse = verifiableCredentialsClient.requestVCI(uri, format)
        _vcContent.value = requestVCIResponse.toString()
    }

    fun getAllCredentials(context: Context) {
        var verifiableCredentialsClient = VerifiableCredentialsClient(context, "218232426")
        var allCredentials = verifiableCredentialsClient.getAllCredentials()
        _vcContent.value = allCredentials.toString()
    }

    fun clearVci() {
        _vcContent.value = ""
    }

}