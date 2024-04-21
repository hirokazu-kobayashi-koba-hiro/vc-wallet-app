package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.error.OAuthBadRequestException
import org.idp.wallet.verifiable_credentials_library.error.OAuthError

class OauthRequestValidator(private val parameters: OAuthRequestParameters) {

  fun validate() {
    throwExceptionIfNotFoundRequiredParams()
    throwExceptionIfDuplicateParamsExcludingResource()
  }

  private fun throwExceptionIfNotFoundRequiredParams() {
    if (!parameters.hasClientId()) {
      throw OAuthBadRequestException(OAuthError.NOT_FOUND_REQUIRED_PARAMS, "client_id is required.")
    }
  }

  private fun throwExceptionIfDuplicateParamsExcludingResource() {
    val multiValuedKeys = parameters.getMultiValuedKeys()
    val filtered = multiValuedKeys.filter { it != "resource" }.toList()
    if (filtered.size > 1) {
      val stringBuilder = StringBuilder()
      stringBuilder.append("duplicate key:")
      filtered.forEach {
        stringBuilder.append(it)
        stringBuilder.append(" ")
      }
      throw OAuthBadRequestException(OAuthError.DUPLICATE_KEY, stringBuilder.toString())
    }
  }
}
