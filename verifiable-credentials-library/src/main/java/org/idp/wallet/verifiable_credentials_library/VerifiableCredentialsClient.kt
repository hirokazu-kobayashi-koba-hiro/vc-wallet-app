package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import androidx.room.Room
import org.idp.wallet.verifiable_credentials_library.activity.VerifiableCredentialsActivity
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifierConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.repository.AppDatabase
import org.idp.wallet.verifiable_credentials_library.repository.CredentialIssuanceResultDataSource
import org.idp.wallet.verifiable_credentials_library.repository.UserDataSource
import org.idp.wallet.verifiable_credentials_library.repository.VerifiableCredentialRecordDataSource
import org.idp.wallet.verifiable_credentials_library.repository.WalletClientConfigurationDataSource
import org.idp.wallet.verifiable_credentials_library.util.jose.JoseUtils
import org.idp.wallet.verifiable_credentials_library.util.store.KeyStore

object VerifiableCredentialsClient {

  fun initialize(context: Context, configuration: WalletConfiguration) {
    val keyStore = KeyStore(context)
    val database =
        Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "verifiable_credential",
            )
            .build()
    val verifiableCredentialRecordDataSource = VerifiableCredentialRecordDataSource(database)
    val walletClientConfigurationDataSource = WalletClientConfigurationDataSource(database)
    val credentialIssuanceResultDataSource = CredentialIssuanceResultDataSource(database)
    val userDataSource = UserDataSource(database)
    val verifiableCredentialsService =
        VerifiableCredentialsService(
            verifiableCredentialRecordDataSource,
            walletClientConfigurationDataSource,
            credentialIssuanceResultDataSource)
    configuration.privateKey = getOrGenerateEcKey(keyStore)
    VerifiableCredentialsApi.initialize(configuration, verifiableCredentialsService)
    OpenIdConnectApi.initialize(userDataSource)
    val mock = VerifierConfigurationRepository {
      return@VerifierConfigurationRepository ClientConfiguration()
    }
    VerifiablePresentationApi.initialize(
        verifiableCredentialRecordDataSource,
        VerifiablePresentationRequestContextService(configuration, mock))
  }

  fun start(context: Context, request: OpenIdConnectRequest, forceLogin: Boolean = false) {
    VerifiableCredentialsActivity.start(
        context = context, request = request, forceLogin = forceLogin)
  }

  private fun getOrGenerateEcKey(keyStore: KeyStore): String {
    val keyId = "vc_wallet_jwt_key"
    val key = keyStore.find(keyId)
    key?.let {
      return it
    }
    val ecKey = JoseUtils.generateECKey(keyId)
    keyStore.store(keyId, ecKey)
    return ecKey
  }
}
