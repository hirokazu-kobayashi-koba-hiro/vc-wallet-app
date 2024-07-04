package org.idp.wallet.verifiable_credentials_library.domain.wallet

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.signer.KPSigner
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WalletCredentialsManagerTest {

  @Test
  fun createAndRestore() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().getContext()
    val walletCredentialsManager = WalletCredentialsManager(File("./"), EncryptedDataStore(context))
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

    val jwt = JWTTools()
    // ...
    val payload = mapOf("claims" to mapOf("name" to "R Daneel Olivaw"))

    println("toHexPublicKey")
    println(credentials.toHexPublicKey())
    println("toHexPrivateKey")
    println(credentials.toHexPrivateKey())

    val signer = KPSigner(credentials.toHexPrivateKey())
    val issuerDID = "did:ethr:${signer.getAddress()}"

    val token = jwt.createJWT(payload, issuerDID, signer)
    println(token)
  }
}
