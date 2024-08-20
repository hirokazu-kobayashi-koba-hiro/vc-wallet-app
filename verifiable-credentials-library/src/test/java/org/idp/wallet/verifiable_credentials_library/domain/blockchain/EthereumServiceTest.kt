package org.idp.wallet.verifiable_credentials_library.domain.blockchain

import org.idp.wallet.verifiable_credentials_library.domain.cert.blockchain.EthereumService
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
  fun issueTransaction() {
    val transactionId =
        EthereumService.issueTransaction(address, privateKey, chain, "blockchainData.toString()")
    println(transactionId)
  }
}
