package org.idp.wallet.verifiable_credentials_library.domain.verifiable_presentation

import org.idp.wallet.verifiable_credentials_library.domain.error.OAuthBadRequestException
import org.idp.wallet.verifiable_credentials_library.domain.error.OAuthError

class VerifiablePresentationRequestValidator(
    private val parameters: VerifiablePresentationRequestParameters
) {

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
