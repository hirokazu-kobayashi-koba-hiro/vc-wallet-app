package org.idp.wallet.verifiable_credentials_library.domain.wallet

import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.Credentials

class WalletCredentials(val credentials: Credentials, val bip39Wallet: Bip39Wallet) {}
