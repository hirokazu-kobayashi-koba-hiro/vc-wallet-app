package org.idp.wallet.verifiable_credentials_library.domain.blockchain

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EthereumServiceTest {
  val url = System.getenv("WEB3_URL") ?: ""
  val address = System.getenv("ADDRESS") ?: ""
  val privateKey = System.getenv("PRIVATE_KEY") ?: ""
  val chain = "ethereum_sepolia"
  val verificationMethod = System.getenv("VERIFICATION_METHOD") ?: ""

  @Before
  fun init() {
    EthereumService.init(url)
  }

  @Test
  fun issueTransactionAndGetTransaction() {
    val data = "data"
    val transactionId = EthereumService.issueTransaction(address, privateKey, chain, data)
    println(transactionId)
    assertNotNull(transactionId)
    val transaction = EthereumService.getTransaction(transactionId)
    println(transaction)
    assertNotNull(transaction)
  }
}
