package org.idp.wallet.app

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VerifiableCredentialIssuanceViewModel :
    ViewModel() {

    var _vcContent = MutableStateFlow("")
    val vciState = _vcContent.asStateFlow()
    suspend fun request(context: Context, uri: String, pinCode: String) {
        val verifiedIdClient = VerifiedIdClientBuilder(context).build()
        val verifiedIdRequestUrl = VerifiedIdRequestURL(Uri.parse(uri))
        val verifiedIdRequestResult: Result<VerifiedIdRequest<*>> =
            verifiedIdClient.createRequest(verifiedIdRequestUrl)

        if (verifiedIdRequestResult.isSuccess) {
            val verifiedIdRequest = verifiedIdRequestResult.getOrNull()
            verifiedIdRequest?.let {
                val requirement = it.requirement
                val requirementList =
                    if (requirement !is GroupRequirement) listOf(requirement) else requirement.requirements
                requirementList.forEach {
                    if (it is PinRequirement) {
                        it.fulfill(pinCode)
                    }
                }
            }
            verifiedIdRequest?.complete()?.fold(
                onSuccess = { issuedVerifiedId ->
                    val verifiedId = issuedVerifiedId as VerifiedId
                    val encodeResult = verifiedIdClient.encode(verifiedId)
                    encodeResult.fold(
                        onSuccess = {
                            it.let { encodedVerifiedId ->
                                _vcContent.value = encodedVerifiedId
                                Toast.makeText(context, encodedVerifiedId, Toast.LENGTH_LONG).show()
                            }
                        },
                        onFailure = {
                            Toast.makeText(context, "encodedVerifiedId failed", Toast.LENGTH_LONG)
                                .show()
                        }
                    )
                },
                onFailure = {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            // If an exception occurs, its value can be accessed here.
            val exception = verifiedIdRequestResult.exceptionOrNull()
        }
    }



}