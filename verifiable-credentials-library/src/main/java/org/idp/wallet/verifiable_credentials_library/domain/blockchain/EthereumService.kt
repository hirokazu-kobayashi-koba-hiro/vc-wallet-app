package org.idp.wallet.verifiable_credentials_library.domain.blockchain

import android.util.Log
import java.math.BigInteger

object EthereumService {

  private lateinit var web3: Web3Wrapper

  fun init(url: String) {
    web3 = Web3Wrapper(url)
  }

  fun issueTransaction(address: String, privateKey: String, chain: String, data: String): String {
    val balance = web3.getBalance(address)
    if (balance < BigInteger.valueOf(20000000)) {
      Log.w("VcWalletLibrary", "balance is less than 20000000")
      throw RuntimeException("balance is less than gas price")
    }
    // FIXME specify toAddress
    val toAddress = "0xdeaddeaddeaddeaddeaddeaddeaddeaddeaddead"
    val transaction = web3.createTransaction(address, toAddress, data, chain)
    val signedTransaction = web3.signTransaction(transaction, privateKey)
    val transactionHash = web3.sendSignedTransaction(signedTransaction)
    return transactionHash
  }
}
