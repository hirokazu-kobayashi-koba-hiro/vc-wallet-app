package org.idp.wallet.verifiable_credentials_library.sdjwt


import id.walt.sdjwt.SDJwt
import org.junit.Assert
import org.junit.Test

class SdJwtTest {

    @Test
    fun success_parse_sdjJwt() {
        val rawSdJwt = "eyJraWQiOiJKMUZ3SlA4N0M2LVFOX1dTSU9tSkFRYzZuNUNRX2JaZGFGSjVHRG5XMVJrIiwidHlwIjoidmMrc2Qtand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJodHRwczovL3RyaWFsLmF1dGhsZXRlLm5ldCIsIl9zZCI6WyItZng2bjBJTGdoQzUzZXBxTEpGenlBLTU4RUFvSFI4dzVfZ0hDeW1VZlZnIiwiNG5OQjBmWllmUEY1elN2REVnLVlpTmtzMDhfTzA3Wm4wTmxkY0dWeWRiTSIsIkFSX1pQZnNaLU5aakowbWtLdVAwTkRyeWxrcHRDZWhVOF9JY3lnMlZGbFEiLCJCQzBzQnlxMzNobW1jQ2prQzFDbkxNNGVzeFdaWGFYZVliWXpDdmhVZUNFIiwiQzVMRElZLXZJaDlwblhlNi1qVjFZYkZMY1lISEV5enBVN2dPV3ZpSXdYNCIsIkRid0VKUzBxT3AzQVZrTVBQWXZLUkJNdHdJVXEwYXdQWFdpUzNKR0QxdUUiLCJGeWQtQjRHVVZBbjczNU1QVnJmNzRNTGVaVG1hbmtQcHRKcXczVF82QXZRIiwiVF92SUF4eXpKODQ0LVNXVzJPOFJFOTdDNkhrdU5qWWFVUkMxdHRwbGQwZyIsImJtTkcwVC1XZVdIVllWZVBzRVRaaDA4Z1ZLSjJuYVJMVE9lSHpFWE9IdjQiLCJuWjNyN2k0UmtuY28zNnZOQlZ6TzdaUDVVbUYtc0YwcWJ0eTBKbng5QTEwIiwicW51MVBuS2NZbkxyaDcyeEhKOTJBOHJJUVZZXzdhQnp6RXN3VkRxamw1TSIsInNRS0pSX3JvQ0ZfNnZ3R1dERlFQdDQwSjBnZ2lkd05pcmlpVUZLQm55VEEiXSwiaWF0IjoxNzEwNzY3NTgwLCJ2Y3QiOiJodHRwczovL2NyZWRlbnRpYWxzLmV4YW1wbGUuY29tL2lkZW50aXR5X2NyZWRlbnRpYWwiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.H5cTWhKJYA94PV1ENVz0OIEGT7pDH14G-7PGzycip5Uh58cE-p3H8kFAe144vOolOa2DZkNbhd_ZCiPxp5w5ZA~WyJlSkhFNEhQNFlGdUd2RzBZZW44cjlBIiwic3ViIiwiMTAwMSJd~WyIxUDFmRklTZVViR2xWYlRMMUxVQWZBIiwiZ2l2ZW5fbmFtZSIsIkpvaG4iXQ~WyJmcnlueW43UEZhTXdST0pCWWZscGZnIiwiZmFtaWx5X25hbWUiLCJTbWl0aCJd~WyIxeXJyXzlHMVNqa1R5MDdZRnBudjN3IiwiYmlydGhkYXRlIiwiMDAwMC0wMy0yMiJd~"
        val sdJwt = SDJwt.parse(rawSdJwt)
        val fullPayload = sdJwt.fullPayload
        val stringBuilder = StringBuilder()
        fullPayload.forEach {
            stringBuilder.append(it.key + ":" + it.value)
            stringBuilder.append("\n")
        }
        println(sdJwt.keyID)
        println(sdJwt.algorithm)
        Assert.assertEquals("J1FwJP87C6-QN_WSIOmJAQc6n5CQ_bZdaFJ5GDnW1Rk", sdJwt.keyID)
        Assert.assertEquals("ES256", sdJwt.algorithm)
    }
}