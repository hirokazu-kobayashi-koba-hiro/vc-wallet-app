package org.idp.wallet.verifiable_credentials_library.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.VerifiableCredentialsRecord
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifiableCredentialRecordDataSourceTest {

  private lateinit var database: AppDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `can save credential`() = runBlocking {
    val dataSource = VerifiableCredentialRecordDataSource(database)
    val record =
        VerifiableCredentialsRecord(
            id = "id",
            issuer = "issuer",
            type = "type",
            format = "format",
            rawVc = "rawVc",
            payload = mapOf("iss" to "iss"))
    dataSource.register("1", record)
  }
}
