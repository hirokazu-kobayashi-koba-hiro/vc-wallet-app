package org.idp.wallet.verifiable_credentials_library.domain.wallet

import java.io.File
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.WalletUtils

object WalletCredentialsManager {

  fun create(password: String): WalletCredentials {
    val bip39Wallet = WalletUtils.generateBip39Wallet(password, File("./"))
    val mnemonic = bip39Wallet.mnemonic
    val credentials = WalletUtils.loadBip39Credentials(password, mnemonic)

    return WalletCredentials(credentials, bip39Wallet)
  }

  fun restore(password: String, mnemonic: String): WalletCredentials {
    val credentials = WalletUtils.loadBip39Credentials(password, mnemonic)
    val fileName =
        WalletUtils.generateWalletFile(password, credentials.ecKeyPair, File("./"), false)
    val bip39Wallet = Bip39Wallet(fileName, mnemonic)
    return WalletCredentials(credentials, bip39Wallet)
  }
}
