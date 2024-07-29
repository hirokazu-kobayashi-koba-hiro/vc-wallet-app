package org.idp.wallet.verifiable_credentials_library.repository

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.idp.wallet.verifiable_credentials_library.domain.configuration.ClientConfiguration
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.WalletClientConfigurationRepository
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class WalletClientConfigurationDataSource(db: AppDatabase) : WalletClientConfigurationRepository {

  val dao = db.walletClientConfigurationDao()

  override suspend fun register(issuer: String, configuration: ClientConfiguration) =
      withContext(Dispatchers.IO) {
        val entity =
            WalletClientConfigurationEntity(
                configuration.clientId, issuer, JsonUtils.write(configuration))
        dao.insert(entity)
      }

  override suspend fun find(issuer: String): ClientConfiguration? =
      withContext(Dispatchers.IO) {
        val entity = dao.selectBy(issuer) ?: return@withContext null
        return@withContext JsonUtils.read(
            entity.payload, ClientConfiguration::class.java, snakeCase = false)
      }
}

@Dao
interface WalletClientConfigurationDao {

  @Insert fun insert(entity: WalletClientConfigurationEntity)

  @Query("SELECT * FROM wallet_client_configuration WHERE issuer = :issuer")
  fun selectBy(issuer: String): WalletClientConfigurationEntity?
}

@Entity("wallet_client_configuration")
class WalletClientConfigurationEntity(
    @PrimaryKey val clientId: String,
    val issuer: String,
    val payload: String,
)
