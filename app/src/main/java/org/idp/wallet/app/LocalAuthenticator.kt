package org.idp.wallet.app

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocalAuthenticator {

  suspend fun authenticate(fragmentActivity: FragmentActivity): Boolean =
      suspendCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val biometricPrompt =
            BiometricPrompt(
                fragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    continuation.resume(false)
                  }

                  override fun onAuthenticationSucceeded(
                      result: BiometricPrompt.AuthenticationResult
                  ) {
                    super.onAuthenticationSucceeded(result)
                    continuation.resume(true)
                  }
                })

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()
        biometricPrompt.authenticate(promptInfo)
      }
}
