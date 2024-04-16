package org.idp.wallet.app

import android.util.Log
import androidx.lifecycle.ViewModel
import id.walt.sdjwt.SDJwt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsRecords

class VerifiableCredentialIssuanceViewModel :
    ViewModel() {

    var _vcContent = MutableStateFlow(mapOf<String, VerifiableCredentialsRecords>())
    val vciState = _vcContent.asStateFlow()
    suspend fun request(uri: String, format: String) {
        val requestVCIResponse = VerifiableCredentialsClient.requestVCI(uri, format)
        Log.d("Vc library app", requestVCIResponse.toString())
    }

    fun getAllCredentials() {
        val allCredentials = VerifiableCredentialsClient.getAllCredentials()
        _vcContent.value = allCredentials
    }

    fun parseSdJwt(value: String): SDJwt {
        try {
            return VerifiableCredentialsClient.parseSdJwt(value)
        } catch (e: Exception) {
            Log.e("Vc library app", e.message?: "failed parseSdJwt")
            throw e
        }
    }

}