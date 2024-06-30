package org.idp.wallet.verifiable_credentials_library.domain.wallet

import java.io.File
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils
import org.idp.wallet.verifiable_credentials_library.util.store.EncryptedDataStoreInterface
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils

class WalletCredentialsManager(
    private val file: File,
    private val encryptedDataStoreInterface: EncryptedDataStoreInterface
) {

  fun create(password: String): WalletCredentials {
    val bip39Wallet = WalletUtils.generateBip39Wallet(password, file)
    val mnemonic = bip39Wallet.mnemonic
    val credentials = WalletUtils.loadBip39Credentials(password, mnemonic)
    encryptedDataStoreInterface.store("credentials", JsonUtils.write(credentials))
    return WalletCredentials(credentials, bip39Wallet)
  }

  fun find(): Credentials? {
    val credentials = encryptedDataStoreInterface.find("credentials")
    credentials?.let {
      return JsonUtils.read(it, Credentials::class.java)
    }
    return null
  }

  fun delete() {
    encryptedDataStoreInterface.delete("credentials")
  }

  fun restore(password: String, mnemonic: String): WalletCredentials {
    val credentials = WalletUtils.loadBip39Credentials(password, mnemonic)
    val fileName = WalletUtils.generateWalletFile(password, credentials.ecKeyPair, file, false)
    val bip39Wallet = Bip39Wallet(fileName, mnemonic)
    return WalletCredentials(credentials, bip39Wallet)
  }
}
