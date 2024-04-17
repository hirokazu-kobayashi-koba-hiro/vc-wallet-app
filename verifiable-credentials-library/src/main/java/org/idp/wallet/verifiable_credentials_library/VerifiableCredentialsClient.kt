package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.util.Log
import id.walt.sdjwt.SDJwt
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationService
import org.json.JSONObject
import kotlin.js.ExperimentalJsExport


object VerifiableCredentialsClient {

    private lateinit var verifiableCredentialsService: VerifiableCredentialsService
    private lateinit var verifiablePresentationService: VerifiablePresentationService

    fun init(context: Context, clientId: String) {
        verifiableCredentialsService = VerifiableCredentialsService(context, clientId)
        verifiablePresentationService = VerifiablePresentationService(context)
    }

    suspend fun requestVCI(url: String, format: String = "vc+sd-jwt"): JSONObject {
        return verifiableCredentialsService.requestVCI(url, format)
    }

    fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
        return verifiableCredentialsService.getAllCredentials()
    }

    suspend fun handleVpRequest(url: String) {
        verifiablePresentationService.handleVpRequest(url)
    }

}