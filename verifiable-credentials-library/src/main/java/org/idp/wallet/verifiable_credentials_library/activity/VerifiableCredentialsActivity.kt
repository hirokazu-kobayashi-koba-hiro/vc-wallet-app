package org.idp.wallet.verifiable_credentials_library.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialsClient
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.DefaultVerifiableCredentialInteractor
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation.DefaultVerifiablePresentationInteractor
import org.idp.wallet.verifiable_credentials_library.domain.wallet.WalletCredentialsManager
import org.idp.wallet.verifiable_credentials_library.ui.VerifiableCredentialsApp
import org.idp.wallet.verifiable_credentials_library.ui.viewmodel.VerifiableCredentialsViewModel
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStore

class VerifiableCredentialsActivity : ComponentActivity() {

  var format: String = ""

  private val viewModel: VerifiableCredentialsViewModel by lazy {
    val factory =
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val context = this@VerifiableCredentialsActivity
            val walletCredentialsManager =
                WalletCredentialsManager(context.filesDir, EncryptedDataStore(context))
            return VerifiableCredentialsViewModel(walletCredentialsManager) as T
          }
        }
    ViewModelProvider(this, factory).get(VerifiableCredentialsViewModel::class.java)
  }

  private val launcher =
      this.registerForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult(),
          callback = {
            val result: IntentResult = IntentIntegrator.parseActivityResult(it.resultCode, it.data)

            val barcodeValue = result.contents

            if (null == barcodeValue) {
              Toast.makeText(this, "Read Error", Toast.LENGTH_LONG).show()
              return@registerForActivityResult
            }
            val errorHandler = CoroutineExceptionHandler { _, error ->
              Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            }
            lifecycleScope.launch(errorHandler) {
              if (format == "vp") {
                viewModel.handleVpRequest(
                    this@VerifiableCredentialsActivity,
                    barcodeValue,
                    interactor = DefaultVerifiablePresentationInteractor())
                Toast.makeText(this@VerifiableCredentialsActivity, "Success", Toast.LENGTH_LONG)
                    .show()
                return@launch
              }
              viewModel.requestVcOnPreAuthorization(
                  this@VerifiableCredentialsActivity,
                  barcodeValue,
                  format,
                  DefaultVerifiableCredentialInteractor())
              Toast.makeText(this@VerifiableCredentialsActivity, "Success", Toast.LENGTH_LONG)
                  .show()
            }
          })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    VerifiableCredentialsClient.initialize(this, "218232426")
    setContent {
      VerifiableCredentialsApp(
          viewModel = viewModel,
          resolveQrCode = {
            format = it
            val intent =
                Intent(this@VerifiableCredentialsActivity, PortraitCaptureActivity::class.java)
            intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "Scan a QR code")
            launcher.launch(intent)
          },
      )
    }
    lifecycleScope.launch { viewModel.getAllCredentials() }
  }
}
