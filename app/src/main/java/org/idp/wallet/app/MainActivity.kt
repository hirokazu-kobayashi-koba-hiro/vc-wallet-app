package org.idp.wallet.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.launch
import org.idp.wallet.app.ui.theme.VCWalletAppTheme

class MainActivity : ComponentActivity() {

    var pinCode: String = ""

    private val viewModel: VerifiableCredentialIssuanceViewModel by lazy {
        ViewModelProvider(this).get(VerifiableCredentialIssuanceViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VCWalletAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(viewModel, onClick = {
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
        lifecycleScope.launch {
            viewModel.request(this@MainActivity, barcodeValue, pinCode)
        }
    }
}

@Composable
fun Content(viewModel: VerifiableCredentialIssuanceViewModel, onClick: (pinCode: String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(all= Dp(20.0F))
    ) {
        var pinCode by remember { mutableStateOf("") }
        val vciState = viewModel.vciState.collectAsState()

        TextField(label = { Text(text = "pinCode: ")}, value = pinCode, onValueChange = {
            pinCode = it
        })
        Button(modifier = Modifier.padding(top = Dp(16.0F)), onClick = {
            onClick(pinCode)
        }) {
            Text(text = "scan QR")
        }
        Divider()
        Text(text = "VerifiableCredentials", modifier = Modifier.padding(top = Dp(16.0F)))
        Text(text = vciState.value, modifier = Modifier
            .padding(top = Dp(16.0F))
            .verticalScroll(
                rememberScrollState()
            ))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VCWalletAppTheme {
        Content(viewModel = VerifiableCredentialIssuanceViewModel(), onClick = {})
    }
}