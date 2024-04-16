package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import android.util.Log
import id.walt.sdjwt.SDJwt
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

    @OptIn(ExperimentalJsExport::class)
    fun parseSdJwt(rawSdJwt: String): SDJwt {
        try {
            return SDJwt.parse(rawSdJwt)
        } catch (e: Exception) {
            Log.e("Vc library", e.message?: "failed parseSdJwt")
            throw e
        }
    }

    suspend fun handleVpRequest(url: String) {
        verifiablePresentationService.handleVpRequest(url)
    }

}