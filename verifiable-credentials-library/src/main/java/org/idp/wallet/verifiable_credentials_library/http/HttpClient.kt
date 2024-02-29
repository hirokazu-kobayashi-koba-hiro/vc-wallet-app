package org.idp.wallet.verifiable_credentials_library.http

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.idp.wallet.verifiable_credentials_library.NetworkException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object HttpClient {

    suspend fun post(
        url: String,
        headers: Map<String, String> = mapOf(),
        requestBody: Map<String, Any>? = null
    ): JSONObject {
        return withContext(Dispatchers.IO) {
            try {
                val connection = HttpURLConnectionCreator.create(url, "POST", headers, requestBody)
                return@withContext ResponseResolver.resolve(connection)
            } catch (e: Exception) {
                when (e) {
                    is NetworkException -> throw e
                }
                throw NetworkException("0003", "unknown network error")
            }
        }
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = mapOf(),
    ): JSONObject {
        return withContext(Dispatchers.IO) {
            try {
                val connection = HttpURLConnectionCreator.create(url, "GET", headers)
                return@withContext ResponseResolver.resolve(connection)
            } catch (e: Exception) {
                when (e) {
                    is NetworkException -> throw e
                }
                throw NetworkException("0003", "unknown network error", e)
            }
        }
    }
}

object HttpURLConnectionCreator {
    fun create(
        url: String,
        method: String,
        headers: Map<String, String>,
        requestBody: Map<String, Any>? = null
    ): HttpURLConnection {
        val httpURLConnection = URL(url).openConnection() as HttpURLConnection
        return httpURLConnection.also {
            it.connectTimeout = 30000
            it.readTimeout = 30000
            it.requestMethod = method
            it.doInput = true
            if (!headers.containsKey("content-type")) {
                it.setRequestProperty("content-type", "application/json")
            }
            headers.forEach { header ->
                it.setRequestProperty(header.key, header.value)
            }
            if (headers.getOrDefault("content-type", "") == "application/x-www-form-urlencoded") {
                requestBody?.let { body ->
                    it.doOutput = true
                    val builder = Uri.Builder()
                    body.forEach { param ->
                        builder.appendQueryParameter(param.key, param.value as String)
                    }
                    builder.build().encodedQuery?.let {encodedQuery ->
                        it.outputStream.write(encodedQuery.toByteArray())
                    }
                }

            } else {
                requestBody?.let { body ->
                    it.doOutput = true
                    it.outputStream.write(JSONObject(body).toString().toByteArray())
                }
            }
        }
    }

}

object ResponseResolver {
    fun resolve(connection: HttpURLConnection): JSONObject {
        when (val status = connection.responseCode) {
            in (200..299) -> {
                val message = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("VC Http", message)
                return JSONObject(message)
            }

            in (400..499) -> {
                val message = connection.errorStream.bufferedReader().use { it.readText() }
                Log.d("VC Http", message)
                throw NetworkException("0001", "network client error statusCode: $status, response: $message")
            }

            in (500..599) -> {
                val message = connection.errorStream.bufferedReader().use { it.readText() }
                Log.d("VC Http", message)
                throw NetworkException("0002", "network server error statusCode: $status, response: $message")
            }
        }
        throw NetworkException("999", "unknown network error")
    }
}
