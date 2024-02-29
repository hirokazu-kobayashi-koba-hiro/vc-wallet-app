package org.idp.wallet.app

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient

class VerifiableCredentialIssuanceViewModel :
    ViewModel() {

    var _vcContent = MutableStateFlow("")
    val vciState = _vcContent.asStateFlow()
    suspend fun request(context: Context, uri: String, pinCode: String) {
        var verifiableCredentialsClient = VerifiableCredentialsClient("218232426")
        var requestVCIResponse = verifiableCredentialsClient.requestVCI(uri)
        _vcContent.value = requestVCIResponse.toString()
    }



}