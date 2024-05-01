package org.idp.wallet.verifiable_credentials_library.oauth

import android.net.Uri
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode
import org.idp.wallet.verifiable_credentials_library.type.vp.PresentationSubmission

class AuthorizationResponse(
    val issuer: String,
    val redirectUri: String,
    val responseMode: ResponseMode? = null,
    val responseModeValue: String = "#",
    val vpToken: String,
    val presentationSubmission: PresentationSubmission,
    val idToken: String? = null,
    val state: String? = null,
    val nonce: String? = null
) {

  fun prams(): Map<String, Any> {
    val params = mutableMapOf<String, Any>()
    params.put("vp_token", vpToken)
    params.put("presentation_submission", presentationSubmission.toJsonString())
    idToken?.let { params.put("id_token", it) }
    state?.let { params.put("state", it) }
    nonce?.let { params.put("nonce", it) }
    return params
  }

  fun redirectUriValue(): String {
    val buildUpon = Uri.parse(redirectUri + responseModeValue).buildUpon()
    buildUpon.appendQueryParameter("iss", issuer)
    buildUpon.appendQueryParameter("vp_token", vpToken)
    buildUpon.appendQueryParameter("presentation_submission", presentationSubmission.toJsonString())
    idToken?.let { buildUpon.appendQueryParameter("id_token", it) }
    state?.let { buildUpon.appendQueryParameter("state", it) }
    nonce?.let { buildUpon.appendQueryParameter("nonce", it) }
    return buildUpon.toString()
  }
}
