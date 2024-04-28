package org.idp.wallet.verifiable_credentials_library.oauth

import android.net.Uri
import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationSubmission
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode

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

    fun redirectUriValue(): String {
        val buildUpon = Uri.parse(redirectUri + responseModeValue).buildUpon()
        buildUpon.appendQueryParameter("iss", issuer)
        buildUpon.appendQueryParameter("vp_token", vpToken)
        buildUpon.appendQueryParameter("presentation_submission", presentationSubmission.toJsonString())
        idToken?.let {
            buildUpon.appendQueryParameter("id_token", it)
        }
        state?.let {
            buildUpon.appendQueryParameter("state", it)
        }
        nonce?.let {
            buildUpon.appendQueryParameter(nonce, it)
        }
        return buildUpon.toString()
    }
}
