package org.idp.wallet.verifiable_credentials_library.util.resource

interface ResourceReader {
  fun read(fileName: String): String
}
