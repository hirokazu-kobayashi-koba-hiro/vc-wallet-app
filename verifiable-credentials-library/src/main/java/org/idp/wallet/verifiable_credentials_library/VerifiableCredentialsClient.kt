package org.idp.wallet.verifiable_credentials_library

import android.content.Context
import androidx.room.Room
import org.idp.wallet.verifiable_credentials_library.activity.VerifiableCredentialsActivity
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.configuration.WalletConfigurationService
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResult
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifiablePresentationRequestContextService
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.VerifierConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.repository.AppDatabase
import org.idp.wallet.verifiable_credentials_library.repository.CredentialIssuanceResultDataSource
import org.idp.wallet.verifiable_credentials_library.repository.VerifiableCredentialRecordDataSource
import org.idp.wallet.verifiable_credentials_library.repository.WalletClientConfigurationDataSource
import org.idp.wallet.verifiable_credentials_library.util.resource.AssetsReader
import org.idp.wallet.verifiable_credentials_library.util.store.KeyStore

object VerifiableCredentialsClient {

  fun initialize(context: Context) {
    val keyStore = KeyStore(context)
    val assetsReader = AssetsReader(context)
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
    val walletConfigurationService = WalletConfigurationService(keyStore, assetsReader)
    walletConfigurationService.initialize()
    val verifiableCredentialsService =
        VerifiableCredentialsService(
            walletConfigurationService,
            verifiableCredentialRecordDataSource,
            walletClientConfigurationDataSource,
            credentialIssuanceResultDataSource)
    VerifiableCredentialsApi.initialize(verifiableCredentialsService)
    val mock = VerifierConfigurationRepository {
      return@VerifierConfigurationRepository ClientConfiguration()
    }
    VerifiablePresentationApi.initialize(
        verifiableCredentialRecordDataSource,
        VerifiablePresentationRequestContextService(walletConfigurationService, mock))
  }

  fun start(context: Context, request: OpenIdConnectRequest, forceLogin: Boolean = false) {
    VerifiableCredentialsActivity.start(
        context = context, request = request, forceLogin = forceLogin)
  }

  suspend fun handlePreAuthorization(
      context: Context,
      subject: String,
      url: String,
      interactor: VerifiableCredentialInteractor
  ) {
    VerifiableCredentialsApi.handlePreAuthorization(context, subject, url, interactor)
  }

  suspend fun handleAuthorizationCode(
      context: Context,
      subject: String,
      url: String,
      credentialConfigurationId: String,
  ) {
    VerifiableCredentialsApi.handleAuthorizationCode(
        context, subject, url, credentialConfigurationId)
  }

  suspend fun getAllCredentials(subject: String): Map<String, VerifiableCredentialsRecords> {
    return VerifiableCredentialsApi.getAllCredentials(subject)
  }

  suspend fun handleVpRequest(
      context: Context,
      subject: String,
      url: String,
      interactor: VerifiablePresentationInteractor
  ): Result<Any> {
    return VerifiablePresentationApi.handleRequest(context, subject, url, interactor)
  }

  suspend fun findAllCredentialIssuanceResults(subject: String): List<CredentialIssuanceResult> {
    return VerifiableCredentialsApi.findAllCredentialIssuanceResults(subject)
  }

  suspend fun handleDeferredCredential(
      context: Context,
      subject: String,
      credentialIssuanceResultId: String
  ) {
    VerifiableCredentialsApi.handleDeferredCredential(
        context = context,
        subject = subject,
        credentialIssuanceResultId = credentialIssuanceResultId)
  }
}
