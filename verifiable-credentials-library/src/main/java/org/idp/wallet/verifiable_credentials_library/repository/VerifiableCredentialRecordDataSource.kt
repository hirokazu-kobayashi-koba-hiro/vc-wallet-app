package org.idp.wallet.verifiable_credentials_library.repository

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialRecordRepository
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecords
import org.idp.wallet.verifiable_credentials_library.util.json.JsonUtils

class VerifiableCredentialRecordDataSource(db: AppDatabase) : VerifiableCredentialRecordRepository {

  private val dao = db.verifiableCredentialRecordDao()

  override suspend fun save(record: VerifiableCredentialsRecord) =
      withContext(Dispatchers.IO) {
        val entity =
            VerifiableCredentialRecordEntity(
                id = record.id,
                issuer = record.issuer,
                type = record.type,
                format = record.format,
                rawVc = record.rawVc,
                payload = JsonUtils.write(record.payload))
        dao.insert(entity)
      }

  @SuppressLint("SuspiciousIndentation")
  override suspend fun getAll(): Map<String, VerifiableCredentialsRecords> =
      withContext(Dispatchers.IO) {
        val entities = dao.getAll()
        if (entities.isEmpty()) return@withContext mapOf()
        val records =
            entities
                .map {
                  VerifiableCredentialsRecord(
                      id = it.id,
                      issuer = it.issuer,
                      type = it.type,
                      format = it.format,
                      rawVc = it.rawVc,
                      payload = JsonUtils.read(it.payload, Map::class.java) as Map<String, Any>)
                }
                .toList()
        return@withContext records
            .groupBy { it.issuer }
            .map { it.key to VerifiableCredentialsRecords(it.value) }
            .toMap()
      }

  override suspend fun getAllAsCollection(): VerifiableCredentialsRecords =
      withContext(Dispatchers.IO) {
        val entities = dao.getAll()
        if (entities.isEmpty()) return@withContext VerifiableCredentialsRecords(listOf())
        val records =
            entities
                .map {
                  VerifiableCredentialsRecord(
                      id = it.id,
                      issuer = it.issuer,
                      type = it.type,
                      format = it.format,
                      rawVc = it.rawVc,
                      payload = JsonUtils.read(it.payload, Map::class.java) as Map<String, Any>)
                }
                .toList()
        return@withContext VerifiableCredentialsRecords(records)
      }

  override suspend fun find(issuer: String): VerifiableCredentialsRecords? =
      withContext(Dispatchers.IO) {
        val entity = dao.selectByIssuer(issuer) ?: return@withContext null
        val record =
            VerifiableCredentialsRecord(
                id = entity.id,
                issuer = entity.issuer,
                type = entity.type,
                format = entity.format,
                rawVc = entity.rawVc,
                payload = JsonUtils.read(entity.payload, Map::class.java) as Map<String, Any>)
        return@withContext VerifiableCredentialsRecords(listOf(record))
      }
}

@Dao
interface VerifiableCredentialRecordDao {
  @Insert fun insert(entity: VerifiableCredentialRecordEntity)

  @Update fun update(entity: VerifiableCredentialRecordEntity)

  @Delete fun delete(entity: VerifiableCredentialRecordEntity)

  @Query("SELECT * FROM verifiable_credential_record_entity")
  fun getAll(): List<VerifiableCredentialRecordEntity>

  @Query("SELECT * FROM verifiable_credential_record_entity WHERE id = :id")
  fun selectBy(id: String): VerifiableCredentialRecordEntity?

  @Query("SELECT * FROM verifiable_credential_record_entity WHERE issuer = :issuer")
  fun selectByIssuer(issuer: String): VerifiableCredentialRecordEntity?
}

@Entity(tableName = "verifiable_credential_record_entity")
class VerifiableCredentialRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "issuer") val issuer: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "format") val format: String,
    @ColumnInfo(name = "raw_vc") val rawVc: String,
    @ColumnInfo(name = "payload") val payload: String,
) {}
