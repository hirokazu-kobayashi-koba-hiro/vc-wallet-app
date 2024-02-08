package org.idp.wallet.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.idp.wallet.app.ui.theme.VCWalletAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VCWalletAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(onClick = {
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

        if (null != result) {
            val barcodeValue = result.contents

            if (null == barcodeValue) {
                Toast.makeText(this, "Read Error", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, barcodeValue, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

@Composable
fun Content(onClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            onClick()
        }) {
            Text(text = "scan QR")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VCWalletAppTheme {
        Content(onClick = {})
    }
}