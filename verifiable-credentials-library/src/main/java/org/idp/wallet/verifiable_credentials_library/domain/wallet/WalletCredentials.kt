package org.idp.wallet.verifiable_credentials_library.domain.wallet

import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials
import org.web3j.utils.Numeric

class WalletCredentials(val credentials: Credentials, val bip39Wallet: Bip39Wallet) {

  fun toHexPublicKey(): String {
    return Numeric.toHexString(credentials.ecKeyPair.publicKey.toByteArray())
  }

  fun toHexPrivateKey(): String {
    return Numeric.toHexString(credentials.ecKeyPair.privateKey.toByteArray())
  }
}
