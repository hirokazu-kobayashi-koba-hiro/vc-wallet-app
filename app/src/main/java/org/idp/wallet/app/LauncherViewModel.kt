package org.idp.wallet.app

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
}
