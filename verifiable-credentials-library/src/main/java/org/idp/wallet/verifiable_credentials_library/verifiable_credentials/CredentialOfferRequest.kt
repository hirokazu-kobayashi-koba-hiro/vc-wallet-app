package org.idp.wallet.verifiable_credentials_library.verifiable_credentials

import org.idp.wallet.verifiable_credentials_library.basic.http.extractQueries
import org.idp.wallet.verifiable_credentials_library.basic.http.extractScheme

class CredentialOfferRequest(private val url: String) {
  val scheme: String? = extractScheme(url)
  val params: Map<String, List<String>> = extractQueries(url)
}
