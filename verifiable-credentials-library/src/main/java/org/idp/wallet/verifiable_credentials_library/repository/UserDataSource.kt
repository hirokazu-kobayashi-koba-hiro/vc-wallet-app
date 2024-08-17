package org.idp.wallet.verifiable_credentials_library.repository

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.idp.wallet.verifiable_credentials_library.domain.type.oidc.Address
import org.idp.wallet.verifiable_credentials_library.domain.user.User
import org.idp.wallet.verifiable_credentials_library.domain.user.UserRepository

class UserDataSource(db: AppDatabase) : UserRepository {
  private val userDao = db.userDao()
  private val currentUserDao = db.currentUserDao()

  override suspend fun register(user: User) =
      withContext(Dispatchers.IO) {
        userDao.insert(
            entity =
                UserEntity(
                    id = user.id,
                    sub = user.sub,
                    givenName = user.givenName,
                    familyName = user.familyName,
                    middleName = user.middleName,
                    nickname = user.nickname,
                    preferredUsername = user.preferredUsername,
                    profile = user.profile,
                    picture = user.picture,
                    website = user.website,
                    email = user.email,
                    emailVerified = user.emailVerified,
                    gender = user.gender,
                    birthdate = user.birthdate,
                    zoneinfo = user.zoneinfo,
                    locale = user.locale,
                    phoneNumber = user.phoneNumber,
                    phoneNumberVerified = user.phoneNumberVerified,
                    updatedAt = user.updatedAt))
        val currentUserEntity = CurrentUserEntity(userId = user.id)
        currentUserDao.insert(entity = currentUserEntity)
      }

  override suspend fun getCurrentUser(): User =
      withContext(Dispatchers.IO) {
        val entity = userDao.selectByCurrent()
        return@withContext entity.toUser()
      }

  override suspend fun get(id: String): User =
      withContext(Dispatchers.IO) {
        val entity = userDao.selectBy(id)
        entity ?: throw RuntimeException(String.format("not found user (%s)"))
        return@withContext entity.toUser()
      }

  override suspend fun findAll(): List<User> =
      withContext(Dispatchers.IO) {
        val entityList = userDao.selectAll()
        return@withContext entityList.map { it.toUser() }.toList()
      }

  override suspend fun find(sub: String): User? =
      withContext(Dispatchers.IO) {
        val entity = userDao.selectBy(sub)
        entity ?: return@withContext null
        return@withContext entity.toUser()
      }

  override suspend fun update(user: User) =
      withContext(Dispatchers.IO) {
        userDao.update(
            entity =
                UserEntity(
                    id = user.id,
                    sub = user.sub,
                    givenName = user.givenName,
                    familyName = user.familyName,
                    middleName = user.middleName,
                    nickname = user.nickname,
                    preferredUsername = user.preferredUsername,
                    profile = user.profile,
                    picture = user.picture,
                    website = user.website,
                    email = user.email,
                    emailVerified = user.emailVerified,
                    gender = user.gender,
                    birthdate = user.birthdate,
                    zoneinfo = user.zoneinfo,
                    locale = user.locale,
                    phoneNumber = user.phoneNumber,
                    phoneNumberVerified = user.phoneNumberVerified,
                    address =
                        AddressEntity(
                            formatted = user.address?.formatted,
                            streetAddress = user.address?.streetAddress,
                            locality = user.address?.locality,
                            region = user.address?.region,
                            postalCode = user.address?.postalCode,
                            country = user.address?.country),
                    updatedAt = user.updatedAt))
        val currentUserEntity = CurrentUserEntity(userId = user.id)
        currentUserDao.insert(entity = currentUserEntity)
      }
}

@Dao
interface UserDao {
  @Insert fun insert(entity: UserEntity)

  @Update fun update(entity: UserEntity)

  @Query("SELECT * FROM user_entity WHERE id = :id") fun selectBy(id: String): UserEntity?

  @Query(
      "SELECT * FROM user_entity JOIN current_user_entity ON user_entity.id = current_user_entity.user_id")
  fun selectByCurrent(): UserEntity

  @Query("SELECT * FROM user_entity") fun selectAll(): List<UserEntity>
}

@Dao
interface CurrentUserDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(entity: CurrentUserEntity)

  @Delete fun delete(entity: CurrentUserEntity)
}

@Entity("user_entity")
class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "sub", index = true) val sub: String,
    @ColumnInfo("name") val name: String? = null,
    @ColumnInfo("given_name") val givenName: String? = null,
    @ColumnInfo("family_name") val familyName: String? = null,
    @ColumnInfo("middle_name") val middleName: String? = null,
    @ColumnInfo("nickname") val nickname: String? = null,
    @ColumnInfo("preferred_username") val preferredUsername: String? = null,
    @ColumnInfo("profile") val profile: String? = null,
    @ColumnInfo("picture") val picture: String? = null,
    @ColumnInfo("website") val website: String? = null,
    @ColumnInfo("email") val email: String? = null,
    @ColumnInfo("email_verified") val emailVerified: Boolean? = null,
    @ColumnInfo("gender") val gender: String? = null,
    @ColumnInfo("birthdate") val birthdate: String? = null,
    @ColumnInfo("zoneinfo") val zoneinfo: String? = null,
    @ColumnInfo("locale") val locale: String? = null,
    @ColumnInfo("phone_number") val phoneNumber: String? = null,
    @ColumnInfo("phone_number_verified") val phoneNumberVerified: Boolean? = null,
    @Embedded val address: AddressEntity? = null,
    @ColumnInfo("updated_at") val updatedAt: String? = null
) {
  fun toUser(): User {
    return User(
        id = id,
        sub = sub,
        givenName = givenName,
        familyName = familyName,
        middleName = middleName,
        nickname = nickname,
        preferredUsername = preferredUsername,
        profile = profile,
        picture = picture,
        website = website,
        email = email,
        emailVerified = emailVerified,
        gender = gender,
        birthdate = birthdate,
        zoneinfo = zoneinfo,
        locale = locale,
        phoneNumber = phoneNumber,
        phoneNumberVerified = phoneNumberVerified,
        address = address?.toAddress(),
        updatedAt = updatedAt)
  }
}

data class AddressEntity(
    val formatted: String? = null,
    @ColumnInfo("street_address") val streetAddress: String? = null,
    val locality: String? = null,
    val region: String? = null,
    @ColumnInfo("postal_code") val postalCode: String? = null,
    val country: String? = null
) {
  fun toAddress(): Address {
    return Address(
        formatted = formatted,
        streetAddress = streetAddress,
        locality = locality,
        region = region,
        postalCode = postalCode,
        country = country
    )
  }
}

@Entity(
    "current_user_entity",
    foreignKeys =
        [
            ForeignKey(
                entity = UserEntity::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("user_id"),
                onDelete = ForeignKey.CASCADE)])
class CurrentUserEntity(@PrimaryKey @ColumnInfo("user_id") val userId: String)
