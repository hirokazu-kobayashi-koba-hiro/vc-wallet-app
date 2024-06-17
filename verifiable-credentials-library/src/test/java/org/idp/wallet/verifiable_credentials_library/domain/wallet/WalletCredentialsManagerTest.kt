package org.idp.wallet.verifiable_credentials_library.domain.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WalletCredentialsManagerTest {

  @Test
  fun createAndRestore() {
    val password = "password"
    val credentials = WalletCredentialsManager.create(password)
    println(credentials.credentials.address)
    println(credentials.bip39Wallet.mnemonic)
    assertNotNull(credentials.credentials.address)
    assertNotNull(credentials.bip39Wallet.mnemonic)
    val restoreCredentials =
        WalletCredentialsManager.restore(password, credentials.bip39Wallet.mnemonic)
    assertEquals(credentials.credentials.address, restoreCredentials.credentials.address)
    assertEquals(
        credentials.credentials.ecKeyPair.privateKey,
        restoreCredentials.credentials.ecKeyPair.privateKey)
  }
}
