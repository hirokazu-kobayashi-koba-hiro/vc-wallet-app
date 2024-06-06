package org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials

import org.idp.wallet.verifiable_credentials_library.util.extension.toListAsString
import org.json.JSONObject

class CredentialOfferCreator {

  companion object {
    fun create(jsonObject: JSONObject): org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer {
      val credentialIssuer = jsonObject.getString("credential_issuer")
      val credentialConfigurationIds =
          jsonObject.getJSONArray("credential_configuration_ids").toListAsString()
      val grantsObject = jsonObject.optJSONObject("grants")
      if (grantsObject == null) {
        return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer(
          credentialIssuer,
          credentialConfigurationIds
        )
      }
      val preAuthorizedCodeGrant =
        org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferCreator.Companion.toPreAuthorizedGrant(
          grantsObject
        )
      val authorizedCodeGrant =
        org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOfferCreator.Companion.toAuthorizationCodeGrant(
          grantsObject
        )
      return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.CredentialOffer(
        credentialIssuer = credentialIssuer,
        credentialConfigurationIds = credentialConfigurationIds,
        preAuthorizedCodeGrant = preAuthorizedCodeGrant,
        authorizedCodeGrant = authorizedCodeGrant
      )
    }

    private fun toAuthorizationCodeGrant(jsonObject: JSONObject): org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.AuthorizedCodeGrant? {
      try {
        val authorizedCodeObject = jsonObject.optJSONObject("authorization_code")
        if (authorizedCodeObject == null) {
          return null
        }
        val issuerState = authorizedCodeObject.optString("issuer_state")
        val authorizationServer = authorizedCodeObject.optString("authorization_server")
        return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.AuthorizedCodeGrant(
          issuerState,
          authorizationServer
        )
      } catch (e: Exception) {
        return null
      }
    }

    private fun toPreAuthorizedGrant(jsonObject: JSONObject): org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.PreAuthorizedCodeGrant? {
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
          return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.PreAuthorizedCodeGrant(
            preAuthorizedCode = preAuthorizedCode,
            length = length,
            inputMode = inputMode,
            description = description
          )
        } ?: return org.idp.wallet.verifiable_credentials_library.domain.verifiable_credentials.PreAuthorizedCodeGrant(
          preAuthorizedCode
        )
      } catch (e: Exception) {
        return null
      }
    }
  }
}
