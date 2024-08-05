package org.idp.wallet.verifiable_credentials_library.repository

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResult
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResultRepository
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialIssuanceResultStatus

class CredentialIssuanceResultDataSource(db: AppDatabase) : CredentialIssuanceResultRepository {

  val dao = db.credentialIssuanceResultDao()

  override suspend fun register(credentialIssuanceResult: CredentialIssuanceResult) =
      withContext(Dispatchers.IO) {
        val entity =
            CredentialIssuanceResultEntity(
                id = credentialIssuanceResult.id,
                issuer = credentialIssuanceResult.issuer,
                credential = credentialIssuanceResult.credential,
                transactionId = credentialIssuanceResult.transactionId,
                cNonce = credentialIssuanceResult.cNonce,
                cNonceExpiresIn = credentialIssuanceResult.cNonceExpiresIn,
                notificationId = credentialIssuanceResult.notificationId,
                status = credentialIssuanceResult.status.name)
        dao.insert(entity)
      }

  override suspend fun findAll(): List<CredentialIssuanceResult> =
      withContext(Dispatchers.IO) {
        val entity = dao.selectAll()
        return@withContext entity.map { it.toResult() }
      }

  override suspend fun update(credentialIssuanceResult: CredentialIssuanceResult) =
      withContext(Dispatchers.IO) {
        val entity =
            CredentialIssuanceResultEntity(
                id = credentialIssuanceResult.id,
                issuer = credentialIssuanceResult.issuer,
                credential = credentialIssuanceResult.credential,
                transactionId = credentialIssuanceResult.transactionId,
                cNonce = credentialIssuanceResult.cNonce,
                cNonceExpiresIn = credentialIssuanceResult.cNonceExpiresIn,
                notificationId = credentialIssuanceResult.notificationId,
                status = credentialIssuanceResult.status.name)
        dao.update(entity)
      }

  override suspend fun delete(id: String) = withContext(Dispatchers.IO) { dao.delete(id) }
}

@Dao
interface CredentialIssuanceResultDao {
  @Insert fun insert(entity: CredentialIssuanceResultEntity)

  @Query("SELECT * FROM credential_issuance_result")
  fun selectAll(): List<CredentialIssuanceResultEntity>

  @Update fun update(entity: CredentialIssuanceResultEntity)

  @Query("DELETE FROM credential_issuance_result WHERE id = :id") fun delete(id: String)
}

@Entity(tableName = "credential_issuance_result")
data class CredentialIssuanceResultEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("issuer") val issuer: String,
    @ColumnInfo("credential") val credential: String?,
    @ColumnInfo("transaction_id") val transactionId: String?,
    @ColumnInfo("c_nonce") val cNonce: String?,
    @ColumnInfo("c_nonce_expires_in") val cNonceExpiresIn: Int?,
    @ColumnInfo("notification_id") val notificationId: String?,
    @ColumnInfo("status") val status: String
) {
  fun toResult(): CredentialIssuanceResult {
    return CredentialIssuanceResult(
        id = id,
        issuer = issuer,
        credential = credential,
        transactionId = transactionId,
        cNonce = cNonce,
        cNonceExpiresIn = cNonceExpiresIn,
        notificationId = notificationId,
        status = CredentialIssuanceResultStatus.valueOf(status))
  }
}
