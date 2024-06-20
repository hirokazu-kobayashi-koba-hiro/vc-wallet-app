package org.idp.wallet.verifiable_credentials_library.domain.blockchain

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

object Web3Client {

  private lateinit var web3: Web3j

  fun init(url: String) {
    web3 = Web3j.build(HttpService(url))
  }

  fun issueTransaction(address: String, privateKey: String, chain: String, data: String): String {
    val balance = getBalance(address)
    if (balance < BigInteger.valueOf(20000000)) {
      Log.w("VcWalletLibrary", "balance is less than 20000000")
      throw RuntimeException("balance is less than gas price")
    }
    val transaction = createTransaction(address, data, chain)
    val signedTransaction = signTransaction(transaction, privateKey)
    val transactionHash = sendSignedTransaction(signedTransaction)
    //    val transactionResult = getTransaction(transactionHash)
    return transactionHash
  }

  fun getBalance(address: String): BigInteger {
    val balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
    Log.d("VcWalletLibrary", "getBalance: $balance")
    return balance
  }

  fun sendSignedTransaction(signedTransaction: String): String {
    for (index in 0 until 10) {
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

  fun createTransaction(address: String, data: String, chain: String): RawTransaction {
    val nonce = getNonceByTransactionCount(address)
    val toAddress = "0xdeaddeaddeaddeaddeaddeaddeaddeaddeaddead"
    val transaction =
        RawTransaction.createTransaction(
            nonce,
            BigInteger.valueOf(200000000000),
            BigInteger.valueOf(25000),
            toAddress,
            BigInteger.ZERO,
            data)
    println(transaction)
    return transaction
  }

  fun getNonceByTransactionCount(address: String): BigInteger {
    val nonce =
        web3
            .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
            .send()
            .transactionCount
    Log.d("VcWalletLibrary", "getTransactionCount nonce: $nonce")
    return nonce
  }

  fun getTransaction(transactionHash: String): TransactionReceipt {
    for (index in 0 until 10) {
      val result = web3.ethGetTransactionReceipt(transactionHash).send().transactionReceipt
      if (result.isPresent) {
        return result.get()
      }
      continue
    }
    throw RuntimeException("Transaction receipt not found: $transactionHash")
  }

  fun toChainId(chainValue: String): Int {
    return when (chainValue) {
      "ethereum_mainnet" -> 1
      "ethereum_ropsten" -> 3
      "ethereum_goerli" -> 5
      "ethereum_sepolia" -> 11155111
      else -> throw RuntimeException("UnknownChainError: $chainValue")
    }
  }
}
