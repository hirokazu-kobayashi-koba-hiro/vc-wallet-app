package org.idp.wallet.verifiable_credentials_library.basic.resource

interface ResourceReader {
  fun read(fileName: String): String
}
