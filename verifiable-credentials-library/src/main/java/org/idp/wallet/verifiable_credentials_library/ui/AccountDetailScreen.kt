package org.idp.wallet.verifiable_credentials_library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.UserinfoResponse
import org.idp.wallet.verifiable_credentials_library.ui.theme.VcWalletTheme
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

@Preview
@Composable
fun AccountDetailScreenPreview() {
  val userinfoResponse =
      JsonUtils.read(
          """
        {
          "sub": "google-oauth2|103097077253353955307",
          "given_name": "弘和",
          "family_name": "小林",
          "nickname": "hiro",
          "name": "小林弘和",
          "picture": "https://lh3.googleusercontent.com/a/ACg8ocIWsRI3IAr41PpGw53N98Yp7pyTOwgDTnaqbz8Gretne25jUg=s96-c",
          "updated_at": "2024-07-07T22:46:03.118Z",
          "email": "hiro@gmail.com",
          "email_verified": true
        }

    """
              .trimIndent(),
          UserinfoResponse::class.java)
  AccountDetailScreen(userinfoResponse = userinfoResponse)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(userinfoResponse: UserinfoResponse) {
  VcWalletTheme {
    Scaffold(
        topBar = {
          CenterAlignedTopAppBar(
              title = {
                Text(text = "Account Detail", style = MaterialTheme.typography.displayMedium)
              })
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(paddingValues)
                      .verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(Dp(16.0F)),
              content = {
                userinfoResponse.picture?.let {
                  AsyncImage(
                      model = it,
                      contentDescription = "account picture",
                      modifier = Modifier.height(Dp(100.0F)).width(Dp(100.0F)),
                      contentScale = ContentScale.Crop)
                }
                AccountDetailRow(label = "id", value = userinfoResponse.sub)
                AccountDetailRow(label = "name", value = userinfoResponse.name ?: "")
                AccountDetailRow(label = "givenName", value = userinfoResponse.givenName ?: "")
                AccountDetailRow(label = "familyName", value = userinfoResponse.familyName ?: "")
                AccountDetailRow(label = "middleName", value = userinfoResponse.middleName ?: "")
                AccountDetailRow(label = "nickname", value = userinfoResponse.nickname ?: "")
                AccountDetailRow(
                    label = "preferredUsername", value = userinfoResponse.preferredUsername ?: "")
                AccountDetailRow(label = "profile", value = userinfoResponse.profile ?: "")
                AccountDetailRow(label = "picture", value = userinfoResponse.picture ?: "")
                AccountDetailRow(label = "website", value = userinfoResponse.website ?: "")
                AccountDetailRow(label = "email", value = userinfoResponse.email ?: "")
                AccountDetailRow(
                    label = "emailVerified", value = userinfoResponse.emailVerified.toString())
                AccountDetailRow(label = "gender", value = userinfoResponse.gender ?: "")
                AccountDetailRow(label = "birthdate", value = userinfoResponse.birthdate ?: "")
                AccountDetailRow(label = "zoneinfo", value = userinfoResponse.zoneinfo ?: "")
                AccountDetailRow(label = "locale", value = userinfoResponse.locale ?: "")
                AccountDetailRow(label = "phoneNumber", value = userinfoResponse.phoneNumber ?: "")
                AccountDetailRow(
                    label = "phoneNumberVerified",
                    value = userinfoResponse.phoneNumberVerified.toString())
                AccountDetailRow(label = "updatedAt", value = userinfoResponse.updatedAt ?: "")
              })
        })
  }
}

@Composable
fun AccountDetailRow(label: String, value: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(start = Dp(20.0F), end = Dp(20.0F)),
      horizontalArrangement = Arrangement.SpaceBetween,
      content = {
        Text(
            text = label,
            modifier = Modifier.padding(end = Dp(16.0F)),
            style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
      })
}
