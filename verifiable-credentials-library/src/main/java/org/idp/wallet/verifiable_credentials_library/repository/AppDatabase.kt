package org.idp.wallet.verifiable_credentials_library.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities =
        [
            VerifiableCredentialRecordEntity::class,
            WalletClientConfigurationEntity::class,
            CredentialIssuanceResultEntity::class,
            UserEntity::class,
            CurrentUserEntity::class,
        ],
    version = 2)
abstract class AppDatabase : RoomDatabase() {
  abstract fun verifiableCredentialRecordDao(): VerifiableCredentialRecordDao

  abstract fun walletClientConfigurationDao(): WalletClientConfigurationDao

  abstract fun credentialIssuanceResultDao(): CredentialIssuanceResultDao

  abstract fun userDao(): UserDao

  abstract fun currentUserDao(): CurrentUserDao
}

object MIGRATION_1_2 : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
            CREATE TABLE IF NOT EXISTS `user_entity` (
                `id` TEXT NOT NULL,
                `sub` TEXT NOT NULL,
                `name` TEXT,
                `given_name` TEXT,
                `family_name` TEXT,
                `middle_name` TEXT,
                `nickname` TEXT,
                `preferred_username` TEXT,
                `profile` TEXT,
                `picture` TEXT,
                `website` TEXT,
                `email` TEXT,
                `email_verified` INTEGER,
                `gender` TEXT,
                `birthdate` TEXT,
                `zoneinfo` TEXT,
                `locale` TEXT,
                `phone_number` TEXT,
                `phone_number_verified` INTEGER,
                `updated_at` TEXT,
                PRIMARY KEY(`id`)
            )
        """)
    db.execSQL(
        """
            CREATE TABLE IF NOT EXISTS `current_user_entity` (
                `user_id` TEXT NOT NULL,
                PRIMARY KEY(`user_id`),
                FOREIGN KEY(`user_id`) REFERENCES `user_entity`(`id`) ON DELETE CASCADE
        """
            .trimIndent())
  }
}
