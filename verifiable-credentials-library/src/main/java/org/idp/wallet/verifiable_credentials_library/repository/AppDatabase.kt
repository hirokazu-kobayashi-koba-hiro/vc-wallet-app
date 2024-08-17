package org.idp.wallet.verifiable_credentials_library.repository

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities =
        [
            VerifiableCredentialRecordEntity::class,
            WalletClientConfigurationEntity::class,
            CredentialIssuanceResultEntity::class,
            UserEntity::class,
            CurrentUserEntity::class,
        ],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun verifiableCredentialRecordDao(): VerifiableCredentialRecordDao

  abstract fun walletClientConfigurationDao(): WalletClientConfigurationDao

  abstract fun credentialIssuanceResultDao(): CredentialIssuanceResultDao

  abstract fun userDao(): UserDao

  abstract fun currentUserDao(): CurrentUserDao
}
