package org.idp.wallet.verifiable_credentials_library.oauth

import org.idp.wallet.verifiable_credentials_library.oauth.vp.PresentationSubmission
import org.idp.wallet.verifiable_credentials_library.type.ResponseMode

class AuthorizationResponse(
    val issuer: String,
    val redirectUri: String,
    val responseMode: ResponseMode? = null,
    val responseModeValue: String? = null,
    val vpToken: String,
    val presentationSubmission: PresentationSubmission,
    val idToken: String? = null,
    val state: String? = null,
    val nonce: String? = null
) {}
