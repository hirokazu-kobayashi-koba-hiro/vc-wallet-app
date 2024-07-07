package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import org.idp.wallet.verifiable_credentials_library.util.extension.toListAsString
import org.json.JSONObject

class CredentialOfferCreator {

  companion object {
    fun create(jsonObject: JSONObject): CredentialOffer {
      val credentialIssuer = jsonObject.getString("credential_issuer")
      val credentialConfigurationIds =
          jsonObject.getJSONArray("credential_configuration_ids").toListAsString()
      val grantsObject = jsonObject.optJSONObject("grants")
      if (grantsObject == null) {
        return CredentialOffer(credentialIssuer, credentialConfigurationIds)
      }
      val preAuthorizedCodeGrant = toPreAuthorizedGrant(grantsObject)
      val authorizedCodeGrant = toAuthorizationCodeGrant(grantsObject)
      return CredentialOffer(
          credentialIssuer = credentialIssuer,
          credentialConfigurationIds = credentialConfigurationIds,
          preAuthorizedCodeGrant = preAuthorizedCodeGrant,
          authorizedCodeGrant = authorizedCodeGrant)
    }

    private fun toAuthorizationCodeGrant(jsonObject: JSONObject): AuthorizedCodeGrant? {
      try {
        val authorizedCodeObject = jsonObject.optJSONObject("authorization_code")
        if (authorizedCodeObject == null) {
          return null
        }
        val issuerState = authorizedCodeObject.optString("issuer_state")
        val authorizationServer = authorizedCodeObject.optString("authorization_server")
        return AuthorizedCodeGrant(issuerState, authorizationServer)
      } catch (e: Exception) {
        return null
      }
    }

    private fun toPreAuthorizedGrant(jsonObject: JSONObject): PreAuthorizedCodeGrant? {
      try {
        val preAuthorizationCodeObject =
            jsonObject.optJSONObject("urn:ietf:params:oauth:grant-type:pre-authorized_code")
        if (preAuthorizationCodeObject == null) {
          return null
        }
        val preAuthorizedCode = preAuthorizationCodeObject.getString("pre-authorized_code")
        val txCodeObject = preAuthorizationCodeObject.optJSONObject("tx_code")
        txCodeObject?.let {
          val length = it.optInt("length")
          val inputMode = it.optString("input_mode")
          val description = it.optString("description")
          return PreAuthorizedCodeGrant(
              preAuthorizedCode = preAuthorizedCode,
              length = length,
              inputMode = inputMode,
              description = description)
        } ?: return PreAuthorizedCodeGrant(preAuthorizedCode)
      } catch (e: Exception) {
        return null
      }
    }
  }
}
