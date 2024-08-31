package org.idp.wallet.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel : ViewModel() {

  var _loading = MutableStateFlow(false)
  val loadingState = _loading.asStateFlow()
}
