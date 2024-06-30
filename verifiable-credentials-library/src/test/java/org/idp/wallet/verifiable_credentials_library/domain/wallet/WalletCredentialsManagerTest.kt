package org.idp.wallet.verifiable_credentials_library.domain.wallet

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WalletCredentialsManagerTest {

  @Test
  fun createAndRestore() {
    val context = InstrumentationRegistry.getInstrumentation().getContext()
    val walletCredentialsManager = WalletCredentialsManager(context)
    val password = "password"
    val credentials = walletCredentialsManager.create(password)
    println(credentials.credentials.address)
    println(credentials.bip39Wallet.mnemonic)
    assertNotNull(credentials.credentials.address)
    assertNotNull(credentials.bip39Wallet.mnemonic)
    val restoreCredentials =
        walletCredentialsManager.restore(password, credentials.bip39Wallet.mnemonic)
    assertEquals(credentials.credentials.address, restoreCredentials.credentials.address)
    assertEquals(
        credentials.credentials.ecKeyPair.privateKey,
        restoreCredentials.credentials.ecKeyPair.privateKey)
  }
}
