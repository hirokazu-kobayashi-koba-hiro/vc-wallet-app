package org.idp.wallet.verifiable_credentials_library.json

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import org.junit.Assert
import org.junit.Test

class JsonPathTest {

    @Test
    fun debug1() {
        val json = """{"hello": "world"}"""
        val read = JsonPath.parse(json)?.read<String>("$.hello") // returns "world"
        val somethingelse = JsonPath.parse(json)
            ?.read<String>("$.somethingelse") // returns null since "somethingelse" key not found
        Assert.assertEquals("world", read)
        Assert.assertNull(somethingelse)
    }
}