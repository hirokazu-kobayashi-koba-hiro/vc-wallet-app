import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.idp.wallet.verifiable_credentials_library.VerifiableCredentialRegistry
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifiableCredentialRegistryTest {

    private lateinit var context: Context
    private lateinit var registry: VerifiableCredentialRegistry

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        registry = VerifiableCredentialRegistry(context)
    }

    @Test
    fun testSaveAndFind() {
        val issuer = "issuer"
        val value = "value"

        registry.save(issuer, value)
        val retrievedValue = registry.find(issuer)
        var jsonArray = JSONArray()
        jsonArray.put(value)
        assertEquals(jsonArray, retrievedValue)
    }

    @Test
    fun testGetAll() {
        val issuer1 = "issuer1"
        val value1 = "value1"
        val issuer2 = "issuer2"
        val value2 = "value2"

        registry.save(issuer1, value1)
        registry.save(issuer2, value2)

        val allValues = registry.getAll()

        assertEquals(JSONArray().put(value1), allValues.optJSONArray(issuer1))
        assertEquals(JSONArray().put(value2), allValues.optJSONArray(issuer2))
    }
}
