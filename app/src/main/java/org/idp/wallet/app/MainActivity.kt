package org.idp.wallet.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdPresentationRequest
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.GroupRequirement
import com.microsoft.walletlibrary.requests.requirements.PinRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.app.ui.theme.VCWalletAppTheme

class MainActivity : ComponentActivity() {

    var pinCode: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VCWalletAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(onClick = {
                        pinCode = it
                        val intentIntegrator = IntentIntegrator(this@MainActivity).apply {
                            setPrompt("Scan a QR code")
                            captureActivity = PortraitCaptureActivity::class.java
                        }.initiateScan()
                    })
                }
            }
        }
    }

    @SuppressLint("ShowToast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        val barcodeValue = result.contents

        if (null == barcodeValue) {
            Toast.makeText(this, "Read Error", Toast.LENGTH_LONG).show()
            return
        }
        val errorHandler = CoroutineExceptionHandler { _, error ->
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        }
        this.lifecycleScope.launch(errorHandler) {
            val verifiedIdClient = VerifiedIdClientBuilder(this@MainActivity).build()

            val verifiedIdRequestUrl = VerifiedIdRequestURL(Uri.parse(barcodeValue))
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
                    onSuccess = {issuedVerifiedId ->
                        val verifiedId = issuedVerifiedId as VerifiedId
                        val encodeResult = verifiedIdClient.encode(verifiedId)
                        encodeResult.fold(
                            onSuccess = {
                                it.let { encodedVerifiedId ->
                                    Toast.makeText(this@MainActivity, encodedVerifiedId, Toast.LENGTH_LONG).show()
                                }
                            },
                            onFailure = {
                                Toast.makeText(this@MainActivity, "encodedVerifiedId failed", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onFailure = {
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                // If an exception occurs, its value can be accessed here.
                val exception = verifiedIdRequestResult.exceptionOrNull()
            }
        }

    }
}

@Composable
fun Content(onClick: (pinCode: String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(all= Dp(20.0F))
    ) {
        var pinCode by remember { mutableStateOf("") }
        TextField(label = { Text(text = "pinCode: ")}, value = pinCode, onValueChange = {
            pinCode = it
        })
        Button(modifier = Modifier.padding(top = Dp(16.0F)), onClick = {
            onClick(pinCode)
        }) {
            Text(text = "scan QR")
        }
        Divider()
        Text(text = "")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VCWalletAppTheme {
        Content(onClick = {})
    }
}