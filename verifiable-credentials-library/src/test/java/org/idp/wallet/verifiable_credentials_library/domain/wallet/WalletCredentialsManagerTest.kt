package org.idp.wallet.verifiable_credentials_library.domain.wallet

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WalletCredentialsManagerTest {

  @Test
  fun `create and restore credential`() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().getContext()
    val walletCredentialsManager = WalletCredentialsManager(File("./"), EncryptedDataStore(context))
    val password = "password"
    val credentials = walletCredentialsManager.create("1", password)
    println(credentials.credentials.address)
    println(credentials.bip39Wallet.mnemonic)
    assertNotNull(credentials.credentials.address)
    assertNotNull(credentials.bip39Wallet.mnemonic)
    val restoreCredentials =
        walletCredentialsManager.restore("1", password, credentials.bip39Wallet.mnemonic)
    assertEquals(credentials.credentials.address, restoreCredentials.credentials.address)
    assertEquals(
        credentials.credentials.ecKeyPair.privateKey,
        restoreCredentials.credentials.ecKeyPair.privateKey)
  }
}
