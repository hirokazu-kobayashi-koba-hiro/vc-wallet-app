package org.idp.wallet.verifiable_credentials_library.domain.blockchain

import org.junit.Before
import org.junit.Test

class Web3ClientTest {
  val url = System.getenv("WEB3_URL") ?: ""
  val address = System.getenv("ADDRESS") ?: ""
  val privateKey = System.getenv("PRIVATE_KEY") ?: ""
  val chain = "ethereum_sepolia"

  @Before
  fun init() {
    Web3Client.init(url)
  }

  @Test
  fun getBalance() {
    Web3Client.getBalance(address)
  }

  @Test
  fun issueTransaction() {
    val data = "data"
    Web3Client.issueTransaction(address, privateKey, chain, data)
  }
}
