package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import org.idp.wallet.verifiable_credentials_library.basic.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.configuration.WalletConfigurationReader
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialRegistry
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.verifiable_presentation.VerifiablePresentationService
import org.json.JSONObject


object VerifiableCredentialsClient {

    private lateinit var verifiableCredentialsService: VerifiableCredentialsService
    private lateinit var verifiablePresentationService: VerifiablePresentationService
    private lateinit var walletConfigurationReader: WalletConfigurationReader

    fun init(context: Context, clientId: String) {
        val assetsReader = AssetsReader(context)
        val registry = VerifiableCredentialRegistry(context)
        walletConfigurationReader = WalletConfigurationReader(assetsReader)
        verifiableCredentialsService = VerifiableCredentialsService(registry, clientId)
        verifiablePresentationService = VerifiablePresentationService(registry, walletConfigurationReader)

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