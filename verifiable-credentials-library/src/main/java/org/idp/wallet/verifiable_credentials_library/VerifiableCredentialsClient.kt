package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import androidx.room.Room
import org.idp.wallet.verifiable_credentials_library.activity.VerifiableCredentialsActivity
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.repository.AppDatabase
import org.idp.wallet.verifiable_credentials_library.repository.VerifiableCredentialRecordDataSource
import org.idp.wallet.verifiable_credentials_library.util.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.util.store.KeyStore

object VerifiableCredentialsClient {

  private lateinit var verifiableCredentialsApi: VerifiableCredentialsApi
  private lateinit var verifiablePresentationApi: VerifiablePresentationApi
  private lateinit var walletConfigurationService: WalletConfigurationService

  fun initialize(context: Context, clientId: String) {
    val keyStore = KeyStore(context)
    val assetsReader = AssetsReader(context)
    val database =
        Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "verifiable_credential",
            )
            .build()
    val repository = VerifiableCredentialRecordDataSource(database)
    walletConfigurationService = WalletConfigurationService(keyStore, assetsReader)
    walletConfigurationService.initialize()
    val verifiableCredentialsService = VerifiableCredentialsService(repository, clientId)
    verifiableCredentialsApi = VerifiableCredentialsApi(verifiableCredentialsService)
    val mock = ClientConfigurationRepository {
      return@ClientConfigurationRepository ClientConfiguration()
    }
    verifiablePresentationApi =
        VerifiablePresentationApi(
            repository,
            VerifiablePresentationRequestContextService(walletConfigurationService, mock))
  }

  fun start(context: Context, request: OpenIdConnectRequest, forceLogin: Boolean = false) {
    VerifiableCredentialsActivity.start(
        context = context, request = request, forceLogin = forceLogin)
  }

  suspend fun handlePreAuthorization(
      context: Context,
      url: String,
      format: String = "vc+sd-jwt",
      interactor: VerifiableCredentialInteractor
  ) {
    verifiableCredentialsApi.handlePreAuthorization(context, url, format, interactor)
  }

  suspend fun getAllCredentials(): Map<String, VerifiableCredentialsRecords> {
    return verifiableCredentialsApi.getAllCredentials()
  }

  suspend fun handleVpRequest(
      context: Context,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Any> {
    return verifiablePresentationApi.handleRequest(context, url, interactor)
  }
}
