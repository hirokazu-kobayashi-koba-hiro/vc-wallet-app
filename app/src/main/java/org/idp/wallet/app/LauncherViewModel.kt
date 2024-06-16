package org.idp.wallet.app

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.idp.wallet.verifiable_credentials_library.OpenIdConnectApi
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.OpenIdConnectRequest

class LauncherViewModel : ViewModel() {

  var _loading = MutableStateFlow(false)
  val loadingState = _loading.asStateFlow()

  suspend fun login(fragmentActivity: FragmentActivity, successCallback: () -> Unit) {
    try {
      _loading.value = true
      val authenticator = LocalAuthenticator()
      val result = authenticator.authenticate(fragmentActivity)
      if (result) {
        successCallback()
      }
    } finally {
      _loading.value = false
    }
  }

  suspend fun loginWithOpenIdConnect(
      fragmentActivity: FragmentActivity,
      successCallback: () -> Unit
  ) {
    try {
      _loading.value = true
      val request =
          OpenIdConnectRequest(
              issuer = "https://dev-l6ns7qgdx81yv2rs.us.auth0.com",
              clientId = "sKUsWLY5BCzdXAggk78km7kOjfQP1rWR",
              scope = "openid profile phone email address offline_access",
              state = UUID.randomUUID().toString(),
              nonce = UUID.randomUUID().toString(),
              redirectUri =
                  "org.idp.verifiable.credentials://dev-l6ns7qgdx81yv2rs.us.auth0.com/android/org.idp.wallet.app/callback")
      val response = OpenIdConnectApi.login(fragmentActivity, request, force = false)
      successCallback()
    } finally {
      _loading.value = false
    }
  }
}
