package org.idp.wallet.verifiable_credentials_library.domain.user

interface UserRepository {

  suspend fun register(user: User)

  suspend fun getCurrentUser(): User

  suspend fun get(id: String): User

  suspend fun findAll(): List<User>

  suspend fun find(sub: String): User?

  suspend fun update(user: User)
}
