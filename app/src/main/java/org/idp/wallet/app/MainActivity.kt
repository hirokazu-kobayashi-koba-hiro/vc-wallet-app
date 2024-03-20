package org.idp.wallet.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.idp.wallet.app.ui.theme.VCWalletAppTheme
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.stream.Collectors

class MainActivity : ComponentActivity() {

    var format: String = ""

    private val viewModel: VerifiableCredentialIssuanceViewModel by lazy {
        ViewModelProvider(this).get(VerifiableCredentialIssuanceViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            HomeView(viewModel, onClick = {
                format = it
                val intentIntegrator = IntentIntegrator(this@MainActivity).apply {
                    setPrompt("Scan a QR code")
                    captureActivity = PortraitCaptureActivity::class.java
                }.initiateScan()
            },
                onClickShow = {
                    viewModel.getAllCredentials(this@MainActivity)
                }
            )
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
        lifecycleScope.launch(errorHandler) {
            viewModel.request(this@MainActivity, barcodeValue, format)
            Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_LONG).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    HomeView(viewModel = VerifiableCredentialIssuanceViewModel(), onClick = {}, onClickShow = {})
}

@Composable
fun HomeView(
    viewModel: VerifiableCredentialIssuanceViewModel,
    onClick: (format: String) -> Unit,
    onClickShow: () -> Unit
) {
    val navController = rememberNavController()
    VCWalletAppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(
                viewModel = viewModel,
                onClick = onClick,
                onClickShow = onClickShow,
                navController = navController
            )
        }
    }
}

@Composable
fun MainScreen(
    viewModel: VerifiableCredentialIssuanceViewModel,
    onClick: (format: String) -> Unit,
    onClickShow: () -> Unit,
    navController: NavHostController
) {
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val items = listOf(
                    Screen.Home,
                    Screen.Vc,
                    Screen.Vp
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Log.d("Vc library app", padding.toString())
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel, onClick = onClick, onClickShow = onClickShow)
            }
            composable(Screen.Vc.route) {
                VcScreen(viewModel = viewModel, onClick = onClick, onClickShow = onClickShow)
            }
            composable(Screen.Vp.route) {
                VpScreen(navController)
            }
        }
    }
}

@Composable
fun VcCardComponent(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(16.0F))
    ) {
        Column(
            modifier = Modifier
                .padding(Dp(16.0F))
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(Dp(8.0F)))
            Text(text = content, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: VerifiableCredentialIssuanceViewModel,
    onClick: (pinCode: String) -> Unit,
    onClickShow: () -> Unit
) {
    val vciState = viewModel.vciState.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "")
            Text(text = "VerifiableCredentials")
            IconButton(onClick = onClickShow) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
        val cardList = mutableListOf<Pair<String, String>>()
        val vc = vciState.value
        vc.keys().forEach { key ->
            val jsonArray = vc.getJSONArray(key)
            for (i in 0 until jsonArray.length()) {
                val value = jsonArray.getString(i)
                val sdJwt = viewModel.parseSdJwt(value)
                val stringBuilder = StringBuilder()
                sdJwt.digestedDisclosures.forEach {
                    stringBuilder.append(it.value.key + ":" + it.value.value)
                    stringBuilder.append("\n")
                }
                cardList.add(Pair(key, stringBuilder.toString()))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cardList) { (issuer, content) ->
                VcCardComponent(title = issuer, content = content)
            }
        }
    }
}

@Composable
fun VcScreen(
    viewModel: VerifiableCredentialIssuanceViewModel,
    onClick: (pinCode: String) -> Unit,
    onClickShow: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var format by remember { mutableStateOf("") }
        val vciState = viewModel.vciState.collectAsState()

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RadioButton(selected = format == "vc+sd-jwt", onClick = {
                    format = "vc+sd-jwt"
                })
                Text(text = "vc-sd-jwt")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RadioButton(selected = format == "mso_mdoc", onClick = {
                    format = "mso_mdoc"
                })
                Text(text = "mso_mdoc")
            }
        }
        Row {
            Button(modifier = Modifier.padding(top = Dp(16.0F)), onClick = {
                onClick(format)
            }) {
                Text(text = "scan QR")
            }
        }

    }
}

@Composable
fun VpScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Screen")
    }
}

sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Vc : Screen("vc", "Vc", Icons.Default.Add)
    object Vp : Screen("vp", "Vp", Icons.Default.Share)
}