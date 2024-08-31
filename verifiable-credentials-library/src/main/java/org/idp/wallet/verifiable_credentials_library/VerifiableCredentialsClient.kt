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

/**
 * This singleton object serves as the client for managing verifiable credentials. It provides
 * methods to initialize the necessary components and start activities related to verifiable
 * credentials.
 */
object VerifiableCredentialsClient {

  /**
   * Initializes the VerifiableCredentialsClient with the necessary configuration and context.
   *
   * This method sets up the key store, database, data sources, services, and API components
   * required for managing verifiable credentials.
   *
   * @param context the application context
   * @param configuration the wallet configuration containing necessary keys and settings
   */
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

  /**
   * Starts the VerifiableCredentialsActivity to handle OpenID Connect requests.
   *
   * This method launches an activity that processes verifiable credential operations based on the
   * provided OpenID Connect request.
   *
   * @param context the application context
   * @param request the OpenID Connect request to be processed
   * @param forceLogin a boolean flag indicating whether to force the login process (default is
   *   false)
   */
  fun start(context: Context, request: OpenIdConnectRequest, forceLogin: Boolean = false) {
    VerifiableCredentialsActivity.start(
        context = context, request = request, forceLogin = forceLogin)
  }

  /**
   * Retrieves or generates an EC key for JWT signing.
   *
   * This method checks if a key with the specified key ID exists in the key store. If it exists,
   * the key is returned. Otherwise, a new EC key is generated, stored in the key store, and then
   * returned.
   *
   * @param keyStore the key store where the key is stored or retrieved
   * @return the EC key as a string
   */
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
