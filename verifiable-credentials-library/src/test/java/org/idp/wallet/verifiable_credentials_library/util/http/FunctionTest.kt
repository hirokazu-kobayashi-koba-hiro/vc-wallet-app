package org.idp.wallet.verifiable_credentials_library.util.http

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FunctionTest {

  @Test
  fun to_extract_queries() {
    val url = "https://example.com/?client_id=123&response_type=vp_token"
    val queries = extractQueries(url)
    Assert.assertEquals(listOf("123"), queries.get("client_id"))
    Assert.assertEquals(listOf("vp_token"), queries.get("response_type"))
  }
}
