package org.idp.wallet.verifiable_credentials_library.domain.user

interface UserRepository {

  suspend fun register(user: User)

  suspend fun getCurrentUsr(): User

  suspend fun get(id: String): User

  suspend fun findALl(): List<User>

  suspend fun find(sub: String): User?

  suspend fun update(user: User)
}
