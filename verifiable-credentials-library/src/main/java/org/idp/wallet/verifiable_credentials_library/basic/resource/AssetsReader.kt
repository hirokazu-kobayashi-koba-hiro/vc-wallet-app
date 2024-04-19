package org.idp.wallet.verifiable_credentials_library.basic.resource

import android.content.Context
import java.io.InputStream

class AssetsReader(private val context: Context) : ResourceReader {

  override fun read(fileName: String): String {
    val executor: (InputStream) -> String = { inputStream ->
      inputStream.bufferedReader().use { it.readText() }
    }
    return context.assets.open(fileName).use(executor)
  }
}
