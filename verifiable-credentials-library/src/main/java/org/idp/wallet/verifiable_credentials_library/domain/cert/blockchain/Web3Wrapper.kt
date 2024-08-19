package org.idp.wallet.verifiable_credentials_library.domain.cert.blockchain

import android.util.Log
import java.math.BigInteger
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric

class Web3Wrapper(url: String) {

  private val web3 = Web3j.build(HttpService(url))

  fun getBalance(address: String): BigInteger {
    val balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
    Log.d("VcWalletLibrary", "getBalance: $balance")
    return balance
  }

  fun sendSignedTransaction(signedTransaction: String, retryCount: Int = 10): String {
    for (index in 0 until retryCount) {
      try {
        println("VcWalletLibrary hexRawTransaction: $signedTransaction")
        val sendResult = web3.ethSendRawTransaction(signedTransaction).send()
        Log.d("VcWalletLibrary", "success sendSignedTransaction")
        if (sendResult.hasError()) {
          println("VcWalletLibrary sendSignedTransaction is error, ${sendResult.error.message}")
          continue
        }
        return sendResult.transactionHash
      } catch (e: Exception) {
        Log.w("VcWalletLibrary", "Warning: ${e.message}")
      }
    }
    throw RuntimeException("failed sendSignedTransaction, please retry")
  }

  fun signTransaction(transaction: RawTransaction, privateKey: String): String {
    val credentials = Credentials.create(privateKey)
    val signedMessage = TransactionEncoder.signMessage(transaction, credentials)
    val signedTransaction = Numeric.toHexString(signedMessage)
    println("signedTransaction: $signedTransaction")
    return signedTransaction
  }

  fun createTransaction(
      fromAddress: String,
      toAddress: String,
      data: String,
      chain: String
  ): RawTransaction {
    val nonce = getLatestTransactionCount(fromAddress)
    val transaction =
        RawTransaction.createTransaction(
            nonce,
            BigInteger.valueOf(200000000000),
            BigInteger.valueOf(25000),
            toAddress,
            BigInteger.ZERO,
            data,
            BigInteger.ZERO,
            BigInteger.ZERO)
    println(transaction)
    return transaction
  }

  fun getLatestTransactionCount(address: String): BigInteger {
    val nonce =
        web3
            .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
            .send()
            .transactionCount
    Log.d("VcWalletLibrary", "getTransactionCount nonce: $nonce")
    return nonce
  }

  fun getTransaction(transactionHash: String): TransactionReceipt? {
    val response = web3.ethGetTransactionReceipt(transactionHash).send().transactionReceipt
    if (response.isPresent) {
      return response.get()
    }
    return null
  }

  private fun toChainId(chainValue: String): Int {
    return when (chainValue) {
      "ethereum_mainnet" -> 1
      "ethereum_ropsten" -> 3
      "ethereum_goerli" -> 5
      "ethereum_sepolia" -> 11155111
      else -> throw RuntimeException("UnknownChainError: $chainValue")
    }
  }
}
